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
import java.util.List;

import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.services.Service;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.name.Names;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.PrivateElements;

public class ServiceModuleRewriter {
	/**
	 * Annotate exposed bindings with Names.named(service.getId()).
	 * Additionally request injection of service object.
	 */
	public static List<Element> annotateExposedWithId(final List<Element> elements, final Service service) {
		final boolean singleton = ReflectionTools.isSingleton(service.getClass());
		return Elements.getElements(new Module() {
			@Override
			public void configure(final Binder binder) {
				final PrivateBinder privateBinder = binder.newPrivateBinder();
				privateBinder.requestInjection(service);

				for (Element element : elements) {
					element.acceptVisitor(new DefaultElementVisitor<Void>() {
						@Override
						public Void visit(PrivateElements privateElements) {
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

		final boolean singleton = ReflectionTools.isSingleton(service.getClass());

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
	 * with {@link ImportService} annotation.
	 * @param service service to scan for {@link ImportService} annotation
	 * @param services list of all services
	 * @param hints list where hints will be stored
	 * @return provided elements with bindings imported from given service
	 */
	public static List<Element> importBindings(final ServiceWrapper service,
			final List<ServiceWrapper> services, final List<String> hints) {
		return Elements.getElements(new Module() {
			@Override
			public void configure(final Binder binder) {
				for (Element element : service.getElements()) {
					element.acceptVisitor(new DefaultElementVisitor<Void>() {
						@Override
						public Void visit(PrivateElements privateElements) {
							PrivateBinder privateBinder = binder.newPrivateBinder();

							importBindings(privateBinder, privateElements, service, services, hints);
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

	private static void importBindings(final PrivateBinder binder, PrivateElements elements, ServiceWrapper service,
			List<ServiceWrapper> services, List<String> hints) {

		// copy all elements
		for (Element element : elements.getElements()) {
			element.applyTo(binder);
		}
		for (Key<?> key : elements.getExposedKeys()) {
			binder.expose(key);
		}

		for (final ServiceWrapper.Dependency dependency : service.getDependencies()) {
			List<Element> importedElements;

			importedElements = dependency.getService().getElements();
			if (importedElements == null) {
				throw new RuntimeException("Imported service does not specify any module");
			}

			for (final Element element : importedElements) {
				element.acceptVisitor(new DefaultElementVisitor<Void>() {
					private <T> void bindKey(Key<T> key) {
						Key<T> sourceKey;

						Annotation annotation = dependency.getAnnotation();
						if (annotation == null) {
							sourceKey = Key.get(key.getTypeLiteral());
						} else {
							sourceKey = Key.get(key.getTypeLiteral(), annotation);
						}

						if (!sourceKey.equals(key)) {
							binder.withSource(element.getSource()).bind(sourceKey).to(key);
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
}
