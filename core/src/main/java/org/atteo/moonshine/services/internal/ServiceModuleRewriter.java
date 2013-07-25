/*
 * Copyright 2013 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.moonshine.services.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.atteo.moonshine.services.ImportBindings;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.services.Services;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.name.Names;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.PrivateElements;

public class ServiceModuleRewriter {
	public static List<Element> annotateExposedWithId(final List<Element> elements, final Service service) {
		final boolean singleton = Services.isSingleton(service.getClass());
		return Elements.getElements(new Module() {
			@Override
			public void configure(final Binder binder) {
				final PrivateBinder privateBinder = binder.newPrivateBinder();
				privateBinder.requestInjection(service);

				for (Element element : elements) {
					element.acceptVisitor(new DefaultElementVisitor<Void>() {
						@Override
						public Void visit(PrivateElements privateElements) {
							PrivateBinder privateBinder = binder.newPrivateBinder().withSource(
									privateElements.getSource());

							annotateExposedWithId(privateBinder, privateElements, service);
							return null;
						}

						@Override
						public <T> Void visit(Binding<T> binding) {
							binding.applyTo(privateBinder);
							Key<T> oldKey = binding.getKey();
							if (!singleton && !Strings.isNullOrEmpty(service.getId())) {
								bindKey(privateBinder, oldKey, Names.named(service.getId()));
							} else {
								privateBinder.expose(oldKey);
							}
							return null;
						}

						@Override
						protected Void visitOther(Element element) {
							element.applyTo(binder);
							return null;
						}
					});
				}
			}
		});
	}

	private static <T> void bindKey(PrivateBinder binder, Key<T> oldKey, Annotation annotation) {
		Key<T> newKey = Key.get(oldKey.getTypeLiteral(), annotation);
		binder.bind(newKey).to(oldKey);
		binder.expose(newKey);
	}

	private static void annotateExposedWithId(final PrivateBinder binder,
			final PrivateElements elements, final Service service) {

		final boolean singleton = Services.isSingleton(service.getClass());

		// rewrite all bindings
		for (Element element : elements.getElements()) {
			element.applyTo(binder);
		}

		// rewrite all exposed bindings to include id
		for (Key<?> oldKey : elements.getExposedKeys()) {
			if (!singleton && !Strings.isNullOrEmpty(service.getId())) {
				bindKey(binder, oldKey, Names.named(service.getId()));
			} else {
				binder.expose(oldKey);
			}
		}
	}

	/**
	 * Create a module with all provided elements and also with bindings imported
	 * with {@link ImportBindings} annotation.
	 * @param elements elements to include in the module
	 * @param service service to scan for {@link ImportBindings} annotation
	 * @param serviceElements map with elements for all services
	 * @param hints list where hints will be stored
	 * @return
	 */
	public static List<Element> importBindings(final List<Element> elements, final Service service,
			final Map<Service, List<Element>> serviceElements, final List<String> hints) {
		return Elements.getElements(new Module() {
			@Override
			public void configure(final Binder binder) {
				for (Element element : elements) {
					element.acceptVisitor(new DefaultElementVisitor<Void>() {
						@Override
						public Void visit(PrivateElements privateElements) {
							PrivateBinder privateBinder = binder.newPrivateBinder().withSource(
									privateElements.getSource());

							importBindings(privateBinder, privateElements, service, serviceElements, hints);
							return null;
						}

						@Override
						protected Void visitOther(Element element) {
							// copy all elements
							element.applyTo(binder);
							return null;
						}
					});
				}
			}
		});
	}

	/**
	 * Find annotation on the field which is itself annotated with {@link BindingAnnotation}.
	 * @param field field with {@link Field#setAccessible(boolean)} already called
	 * @return binding annotation or null, if not found
	 */
	private static Annotation findBindingAnnotation(Field field) {
		Annotation result = null;
		for (Annotation annotation : field.getAnnotations()) {
			if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class)) {
				if (result == null) {
					result = annotation;
				} else {
					throw new RuntimeException("More than one binding annotation specified");
				}
			}
		}
		return result;
	}

	private static void importBindings(final PrivateBinder binder, PrivateElements elements, Service service,
			Map<Service, List<Element>> serviceElements, List<String> hints) {

		// copy all elements
		for (Element element : elements.getElements()) {
			element.applyTo(binder);
		}
		for (Key<?> key : elements.getExposedKeys()) {
			binder.expose(key);
		}

		for (final Field field : service.getClass().getDeclaredFields()) {
			if (!field.isAnnotationPresent(ImportBindings.class)) {
				continue;
			}

			if (!Service.class.isAssignableFrom(field.getType())) {
				throw new RuntimeException("@" + ImportBindings.class.getSimpleName() + " annotation can only"
						+ " be specified on a field of type " + Service.class.getSimpleName());
			}

			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					field.setAccessible(true);
					return null;
				}
			});
			Service importedService;
			try {
				importedService = (Service) field.get(service);
			} catch (IllegalAccessException| IllegalArgumentException e) {
				throw new RuntimeException("Cannot access field", e);
			}

			List<Element> importedElements;

			if (importedService != null) {
				importedElements = serviceElements.get(importedService);
				if (importedElements == null) {
					throw new RuntimeException("Imported service does not specify any module");
				}
			} else {
				importedElements = findDefaultServiceElements(serviceElements, field.getType());
				if (importedElements == null) {
					hints.add("Service '" + service.toString() + "' depends on '" + field.getType().getSimpleName()
							+ "' which is not available");
					importedElements = Collections.emptyList();
				}
			}

			for (Element element : importedElements) {
				element.acceptVisitor(new DefaultElementVisitor<Void>() {
					private <T> void bindKey(Key<T> key) {
						Key<T> sourceKey;

						Annotation annotation = findBindingAnnotation(field);
						if (annotation == null) {
							sourceKey = Key.get(key.getTypeLiteral());
						} else {
							sourceKey = Key.get(key.getTypeLiteral(), annotation);
						}

						if (!sourceKey.equals(key)) {
							binder.bind(sourceKey).to(key);
						}
					}

					@Override
					public Void visit(PrivateElements privateElements) {
						for (Key<?> key : privateElements.getExposedKeys()) {
							bindKey(key);
						}
						return null;
					}

					@Override
					public <T> Void visit(Binding<T> binding) {
						bindKey(binding.getKey());
						return null;
					}
				});
			}
		}
	}

	private static List<Element> findDefaultServiceElements(Map<Service, List<Element>> serviceElements,
			Class<?> type) {

		List<Element> result = null;
		for (Map.Entry<Service, List<Element>> entry : serviceElements.entrySet()) {
			Service service = entry.getKey();
			List<Element> elements = entry.getValue();

			if (type.isAssignableFrom(service.getClass())) {
				if (result != null) {
					throw new RuntimeException("Could not find unique service of type: " + type);
				}
				result = elements;
			}
		}
		return result;
	}
}
