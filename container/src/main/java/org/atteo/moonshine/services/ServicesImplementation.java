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
package org.atteo.moonshine.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atteo.evo.urlhandlers.UrlHandlers;
import org.atteo.moonshine.ConfigurationException;
import org.atteo.moonshine.injection.InjectMembersModule;
import org.atteo.moonshine.services.internal.DuplicateDetectionWrapper;
import org.atteo.moonshine.services.internal.ReflectionTools;
import org.atteo.moonshine.services.internal.ServiceMetadata;
import org.atteo.moonshine.services.internal.ServiceMetadata.Status;
import static org.atteo.moonshine.services.internal.ServiceMetadata.Status.STARTED;
import org.atteo.moonshine.services.internal.ServiceModuleRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Binding;
import com.google.inject.BindingAnnotation;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.PrivateElements;

public class ServicesImplementation implements Services, Services.Builder {
	private final Logger logger = LoggerFactory.getLogger("Moonshine");
	private final List<Module> extraModules = new ArrayList<>();

	private Injector injector;
	private ServicesConfig config;
	private List<ServiceMetadata> services;

	public ServicesImplementation() {
		UrlHandlers.registerAnnotatedHandlers();
	}

	@Override
	public Builder addModule(Module module) {
		extraModules.add(module);
		return this;
	}

	@Override
	public Builder configuration(ServicesConfig config) {
		this.config = config;
		return this;
	}

	@Override
	public Services build() throws ConfigurationException {
		createInjector();
		return this;
	}

	private Injector buildInjector() throws ConfigurationException {
		List<Module> modules = new ArrayList<>();
		DuplicateDetectionWrapper duplicateDetection = new DuplicateDetectionWrapper();

		// Use ServletModule specifically so @RequestScoped annotation will be always bound
		Module servletsModule = duplicateDetection.wrap(new ServletModule() {
			@Override
			public void configureServlets() {
				binder().requireExplicitBindings();
			}
		});

		// important magic below:
		// Every ServletModule instance tries to install InternalServletModule. The trick is used, because Guice
		// installs modules only the first time and ignores any subsequent execution of install method
		// with the same module (by comparing them using equals() method).
		// We need to make sure InternalServletModule is installed in the top level module,
		// because when it is installed from some private module it doesn't have an access to all
		// registered servlets and filters.
		//
		// The line below makes sure first instance of ServletModule is rewritten in global scope
		modules.add(Elements.getModule(Elements.getElements(servletsModule)));

		for (Module module : extraModules) {
			modules.add(duplicateDetection.wrap(module));
		}

		services = readServiceMetadata(config.getSubServices());
		verifySingletonServicesAreUnique(services);

		for (ServiceMetadata service : services) {
			logger.info("Configuring: {}", service.getName());
			Module module = service.getService().configure();
			if (module != null) {
				service.setElements(Elements.getElements(duplicateDetection.wrap(module)));
			} else {
				service.setElements(Collections.<com.google.inject.spi.Element>emptyList());
			}
		}

		for (ServiceMetadata service : services) {
			checkOnlySingletonBindWithoutAnnotation(service);
		}

		for (ServiceMetadata service : services) {
			service.setElements(ServiceModuleRewriter.annotateExposedWithId(service.getElements(),
					service.getService()));
		}

		List<String> hints = new ArrayList<>();

		for (ServiceMetadata service : services) {
			service.setElements(ServiceModuleRewriter.importBindings(service, services, hints));
		}

		for (ServiceMetadata service : services) {
			modules.add(Elements.getModule(service.getElements()));
		}

		modules.add(new InjectMembersModule());

		try {
			return Guice.createInjector(modules);
		} catch (CreationException e) {
			if (!hints.isEmpty()) {
				logger.warn("Problem detected while creating Guice injector, possible causes:");
				for (String hint : hints) {
					logger.warn(" -> " + hint);
				}
			}
			throw e;
		}
	}

	private void createInjector() throws ConfigurationException {
		logger.info("Building Guice injector hierarchy");

		if (config == null) {
			config = new ServicesConfig();
		}

		injector = buildInjector();
	}

	@Override
	public Injector getGlobalInjector() {
		return injector;
	}

	@Override
	public void start() {
		logger.info("Starting services");
		for (ServiceMetadata service : services) {
			if (logger.isInfoEnabled()
					&& ReflectionTools.isMethodOverriden(service.getService().getClass(), Service.class, "start")) {
				logger.info("Starting: {}", service.getName());
			}
			service.setStatus(STARTED);
			service.getService().start();
		}
		logger.info("All services started");
	}

	@Override
	public void stop() {
		if (config == null) {
			return;
		}

		for (ServiceMetadata service : services) {
			if (service.getStatus() != Status.STARTED) {
				continue;
			}
			String name = service.getName();
			if (name == null) {
				name = service.getClass().getSimpleName();
			}
			logger.info("Stopping: {}", name);
			service.getService().stop();
			service.setStatus(Status.READY);
		}
	}

	@Override
	public void close() {
		stop();
		for (Service service : config.getSubServices()) {
			service.close();
		}
		if (logger != null) {
			logger.info("All services stopped");
		}
		injector = null;
	}

	@Override
	public List<ServiceMetadata> getServiceElements() {
		return services;
	}

	private void verifySingletonServicesAreUnique(List<ServiceMetadata> services) throws ConfigurationException {
		Set<Class<?>> set = new HashSet<>();
		for (ServiceMetadata service : services) {
			Class<?> klass = service.getService().getClass();
			if (service.isSingleton()) {
				if (set.contains(klass)) {
					throw new ConfigurationException("Service '" + service.getName() + "' is marked"
							+ " as singleton, but is declared more than once in configuration file");
				}
				set.add(klass);

				if (!Strings.isNullOrEmpty(service.getService().getId())) {
					throw new ConfigurationException("Service '" + service.getName() + "' is marked"
							+ " as singleton, but has an id specified");
				}
			}
		}
	}

	private static void ensureBindsWithoutAnnotation(Element element, final ServiceMetadata service) {
			element.acceptVisitor(new DefaultElementVisitor<Void>() {
				@Override
				public <T> Void visit(Binding<T> binding) {
					if (binding.getKey().getAnnotation() != null) {
						throw new IllegalStateException("Non singleton service " + service.getName()
								+ " cannot bind " + binding.toString() + ". Only services marked with @Singleton"
								+ " can bind with annotation.");
					}
					return null;
				}

				@Override
				public Void visit(PrivateElements privateElements) {
					for (Key<?> key : privateElements.getExposedKeys()) {
						if (key.getAnnotation() != null) {
							throw new IllegalStateException("Non singleton service " + service.getName()
									+ " cannot expose " + key.toString() + ". Only services marked with @Singleton"
									+ " can expose bindings with annotation.");
						}
					}

					return null;
				}
			});
	}

	private static void checkOnlySingletonBindWithoutAnnotation(final ServiceMetadata service) {
		if (service.isSingleton()) {
			return;
		}
		for (Element element : service.getElements()) {
			ensureBindsWithoutAnnotation(element, service);
		}
	}

	/**
	 * Finds dependencies between services.
	 */
	private List<ServiceMetadata> readServiceMetadata(List<Service> services) throws ConfigurationException {
		List<ServiceMetadata> servicesMetadata = new ArrayList<>();
		Map<Service, ServiceMetadata> map = new IdentityHashMap<>();
		for (Service service : services) {
			ServiceMetadata metadata = new ServiceMetadata(service);
			servicesMetadata.add(metadata);
			map.put(service, metadata);
		}

		for (ServiceMetadata metadata : servicesMetadata) {
			for (Class<? super Service> serviceClass : ReflectionTools.getAncestors(metadata.getService().getClass())) {
				metadata.setSingleton(ReflectionTools.isSingleton(serviceClass));
				for (final Field field : serviceClass.getDeclaredFields()) {
					if (!field.isAnnotationPresent(ImportService.class)) {
						continue;
					}

					if (!Service.class.isAssignableFrom(field.getType())) {
						throw new RuntimeException("@" + ImportService.class.getSimpleName() + " annotation can only"
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
						importedService = (Service) field.get(metadata.getService());
						ServiceMetadata importedServiceMetadata;
						if (importedService == null) {
							importedServiceMetadata = findDefaultService(servicesMetadata, field.getType());
							if (importedServiceMetadata == null) {
								throw new ConfigurationException("Service '" + metadata.getName()
										+ "' requires '" + field.getType().getName() + "' which was not found."
										+ " Please check your configuration files.");
							}

							field.set(metadata.getService(), importedServiceMetadata.getService());
						} else {
							importedServiceMetadata = map.get(importedService);
							if (importedServiceMetadata == null) {
								throw new RuntimeException("Unknown service imported");
							}
						}
						Annotation annotation = findBindingAnnotation(field);
						metadata.addDependency(importedServiceMetadata, annotation);
					} catch (IllegalAccessException| IllegalArgumentException e) {
						throw new RuntimeException("Cannot access field", e);
					}
				}
			}
		}
		return servicesMetadata;
	}

	/**
	 * Find the default service which can be assigned for given type.
	 */
	private static ServiceMetadata findDefaultService(List<ServiceMetadata> services, Class<?> type) {
		ServiceMetadata result = null;
		for (ServiceMetadata service : services) {
			if (type.isAssignableFrom(service.getService().getClass())) {
				if (result != null) {
					throw new RuntimeException("Could not find unique service of type: " + type);
				}
				result = service;
			}
		}
		return result;
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

}
