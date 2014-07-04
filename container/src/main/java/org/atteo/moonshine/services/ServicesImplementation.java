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

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;

import org.atteo.moonshine.ConfigurationException;
import org.atteo.moonshine.injection.InjectMembersModule;
import org.atteo.moonshine.reflection.ReflectionUtils;
import org.atteo.moonshine.services.internal.DuplicateDetectionWrapper;
import org.atteo.moonshine.services.internal.ReflectionTools;
import org.atteo.moonshine.services.internal.ServiceModuleRewriter;
import org.atteo.moonshine.services.internal.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.PrivateElements;

class ServicesImplementation implements Services, Services.Builder {
	private final Logger logger = LoggerFactory.getLogger("Moonshine");
	private final List<Module> extraModules = new ArrayList<>();

	private Injector injector;
	private Service root;
	private final List<LifeCycleListener> listeners = new ArrayList<>();
	private List<ServiceWrapper> services;

	public ServicesImplementation() {
	}

	@Override
	public Builder addModule(Module module) {
		extraModules.add(module);
		return this;
	}

	@Override
	public Builder configuration(Service root) {
		this.root = root;
		return this;
	}

	@Override
	public Builder registerListener(LifeCycleListener listener) {
		listeners.add(listener);
		return this;
	}

	@Override
	public Services build() throws ConfigurationException {
		if (root == null) {
			root = new AbstractService() {
			};
		}

		buildInjector();
		return this;
	}

	private void buildInjector() throws ConfigurationException {
		List<Module> modules = new ArrayList<>();
		DuplicateDetectionWrapper duplicateDetection = new DuplicateDetectionWrapper();

		// Use ServletModule specifically so @RequestScoped annotation will be always bound
		Module servletsModule = duplicateDetection.wrap(new ServletModule() {
			@Override
			public void configureServlets() {
				binder().requireExplicitBindings();
				binder().bind(new TypeLiteral<List<? extends ServiceInfo>>() {})
						.toProvider(new Provider<List<? extends ServiceWrapper>>() {
							@Override
							public List<? extends ServiceWrapper> get() {
								return getServiceElements();
							}
						});
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
		// The line below makes sure the first instance of ServletModule is rewritten in global scope
		modules.add(Elements.getModule(Elements.getElements(servletsModule)));

		for (Module module : extraModules) {
			modules.add(duplicateDetection.wrap(module));
		}

		services = readServiceMetadata(retrieveServicesRecursively(root));
		services = sortTopologically(services);
		verifySingletonServicesAreUnique(services);

		registerInJMX();

		List<String> hints = new ArrayList<>();

		try {
			for (ServiceWrapper service : services) {
				Module module = service.configure();
				if (module != null) {
					service.setElements(Elements.getElements(duplicateDetection.wrap(module)));
				} else {
					service.setElements(Collections.<com.google.inject.spi.Element>emptyList());
				}
			}

			for (ServiceWrapper service : services) {
				checkOnlySingletonBindWithoutAnnotation(service);
			}

			for (ServiceWrapper service : services) {
				service.setElements(ServiceModuleRewriter.annotateExposedWithId(service.getElements(),
						service.getService()));
			}

			for (ServiceWrapper service : services) {
				service.setElements(ServiceModuleRewriter.importBindings(service, services, hints));
			}

			for (ServiceWrapper service : services) {
				modules.add(Elements.getModule(service.getElements()));
			}

			modules.add(new InjectMembersModule());

			logger.info("Creating injector");
			injector = Guice.createInjector(modules);

			for (LifeCycleListener listener : listeners) {
				listener.configured(getGlobalInjector());
			}
		} catch (CreationException e) {
			if (!hints.isEmpty()) {
				logger.warn("Problem detected while creating Guice injector, possible causes:");
				for (String hint : hints) {
					logger.warn(" -> " + hint);
				}
			}
			try {
				close();
			} catch (Exception f) {
				e.addSuppressed(f);
			}
			throw e;
		} catch (RuntimeException e) {
			try {
				close();
			} catch (Exception f) {
				e.addSuppressed(f);
			}
			throw e;
		}
	}

	private void registerInJMX() throws ConfigurationException {
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			for (ServiceWrapper service : services) {
				mbeanServer.registerMBean(service, null);
			}
		} catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
			throw new RuntimeException(e);
		}
	}

	private void unregisterFromJMX() {
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		for (ServiceWrapper service : services) {
			try {
				mbeanServer.unregisterMBean(service.getObjectName());
			} catch (InstanceNotFoundException | MBeanRegistrationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Injector getGlobalInjector() {
		return injector;
	}

	@Override
	public void start() {
		logger.info("Starting services");
		for (ServiceWrapper service : services) {
			service.start();
		}
		logger.info("All services started");
		for (LifeCycleListener listener : listeners) {
			listener.started();
		}
	}

	@Override
	public void stop() {
		for (LifeCycleListener listener : listeners) {
			listener.stopping();
		}
		for (ServiceWrapper service : Lists.reverse(services)) {
			service.stop();
		}
	}

	@Override
	public void close() {
		unregisterFromJMX();
		stop();
		for (LifeCycleListener listener : listeners) {
			listener.closing();
		}
		for (ServiceWrapper service : Lists.reverse(services)) {
			service.close();
		}
		if (logger != null) {
			logger.info("All services stopped");
		}
		injector = null;
	}

	@Override
	public List<? extends ServiceWrapper> getServiceElements() {
		return services;
	}

	private static void verifySingletonServicesAreUnique(List<ServiceWrapper> services) throws ConfigurationException {
		Set<Class<?>> set = new HashSet<>();
		for (ServiceWrapper service : services) {
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

	private static void ensureBindsWithoutAnnotation(Element element, final ServiceWrapper service) {
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

	private static void checkOnlySingletonBindWithoutAnnotation(final ServiceWrapper service) {
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
	private List<ServiceWrapper> readServiceMetadata(List<Service> services) throws ConfigurationException {
		List<ServiceWrapper> servicesMetadata = new ArrayList<>();
		Map<Service, ServiceWrapper> map = new IdentityHashMap<>();
		for (Service service : services) {
			ServiceWrapper metadata = new ServiceWrapper(service);
			servicesMetadata.add(metadata);
			map.put(service, metadata);
		}

		List<String> configurationErrors = new ArrayList<>();

		for (ServiceWrapper metadata : servicesMetadata) {
			for (Class<?> ancestorClass : ReflectionUtils.getAncestors(metadata.getService().getClass())) {
				metadata.setSingleton(ReflectionTools.isSingleton(ancestorClass));
				for (final Field field : ancestorClass.getDeclaredFields()) {
					ImportService importAnnotation = field.getAnnotation(ImportService.class);
					if (importAnnotation == null) {
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
						ServiceWrapper importedServiceMetadata;

						if (importedService == null) {
							try {
								importedServiceMetadata = findDefaultService(servicesMetadata, field.getType());
							} catch (ConfigurationException ex) {
								configurationErrors.add("Service '" + metadata.getName()
								    + "' requires '" + field.getType().getName() + "' which is"
								    + " defined more than once. Please specify an ID in your"
								    + " configuration files.");

								continue;
							}

							if (importedServiceMetadata == null) {
								configurationErrors.add("Service '" + metadata.getName()
								    + "' requires '" + field.getType().getName() + "' which was"
								    + " not found. Please check your configuration files.");

								continue;
							}

							field.set(metadata.getService(), importedServiceMetadata.getService());
						} else {
							importedServiceMetadata = map.get(importedService);
							if (importedServiceMetadata == null) {
								throw new RuntimeException("Unknown service imported");
							}
						}

						metadata.addDependency(importedServiceMetadata, importAnnotation.bindWith());
					} catch (IllegalAccessException | IllegalArgumentException e) {
						throw new RuntimeException("Cannot access field", e);
					}
				}
			}
		}

		if (!configurationErrors.isEmpty()) {
			if (configurationErrors.size() == 1) {
				throw new ConfigurationException(configurationErrors.get(0));
			} else {
				for (String error : configurationErrors) {
					logger.error(error);
				}

				throw new ConfigurationException("Multiple configuration errors");
			}
		}

		return servicesMetadata;
	}

	/**
	 * Find the default service which can be assigned for given type.
	 */
	private static ServiceWrapper findDefaultService(List<ServiceWrapper> services, Class<?> type) throws ConfigurationException {
		ServiceWrapper result = null;
		for (ServiceWrapper service : services) {
			if (type.isAssignableFrom(service.getService().getClass())) {
				if (result != null) {
					throw new ConfigurationException("Service of type '" + type + "' is not unique");
				}
				result = service;
			}
		}
		return result;
	}

	private static List<Service> retrieveServicesRecursively(Service service) {
		List<Service> result = new ArrayList<>();
		addServicesRecursively(result, service);
		return result;
	}

	private static void addServicesRecursively(List<Service> result, Service service) {
		result.add(service);
		for (Service subService : service.getSubServices()) {
			addServicesRecursively(result, subService);
		}
	}

	private static void handleCycle(ServiceWrapper service, List<ServiceWrapper> chain) {
		boolean cycleStarted = false;

		StringBuilder builder = new StringBuilder();
		for (ServiceWrapper serviceWrapper : chain) {
			if (serviceWrapper == service) {
				cycleStarted = true;
			}

			if (cycleStarted) {
				builder.append(serviceWrapper.getName());
				builder.append(" -> ");
			}
		}

		builder.append(service.getName());
		throw new RuntimeException("Service " + service.getName() + " depends on itself: "
		    + builder.toString());
	}

	private static void addService(ServiceWrapper service, Set<ServiceWrapper> set, List<ServiceWrapper> chain,
	    List<ServiceWrapper> sorted) {
		// check for cycles
		if (chain.contains(service)) {
			handleCycle(service, chain);
		}

		if (!set.contains(service)) {
			return;
		}

		chain.add(service);

		for (ServiceWrapper.Dependency dependency : service.getDependencies()) {
			addService(dependency.getService(), set, chain, sorted);
		}

		chain.remove(service);

		set.remove(service);

		sorted.add(service);
	}

	private static List<ServiceWrapper> sortTopologically(List<ServiceWrapper> services) {
		List<ServiceWrapper> sorted = new ArrayList<>();

		Set<ServiceWrapper> set = new LinkedHashSet<>(services);

		while (!set.isEmpty()) {
			ServiceWrapper service = set.iterator().next();
			addService(service, set, new ArrayList<ServiceWrapper>(), sorted);
		}
		return sorted;
	}
}
