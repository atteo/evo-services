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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.urlhandlers.UrlHandlers;
import org.atteo.moonshine.ConfigurationException;
import org.atteo.moonshine.injection.InjectMembersModule;
import org.atteo.moonshine.services.internal.DuplicateDetectionWrapper;
import org.atteo.moonshine.services.internal.ReflectionTools;
import org.atteo.moonshine.services.internal.ServiceModuleRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

public class ServicesImplementation implements Services, Services.Builder {
	private final Logger logger = LoggerFactory.getLogger("Moonshine");
	private final List<Module> extraModules = new ArrayList<>();

	private Injector injector;
	private ServicesConfig config;
	private final List<Service> startedServices = new ArrayList<>();
	private final Map<Service, String> serviceNameMap = new IdentityHashMap<>();
	private final Map<Service, List<com.google.inject.spi.Element>> serviceElements = new LinkedHashMap<>();

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

	private Injector buildInjector() {
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

		for (Service service : config.getSubServices()) {
			logger.info("Configuring: {}", serviceNameMap.get(service));
			Module module = service.configure();
			if (module != null) {
				serviceElements.put(service, Elements.getElements(duplicateDetection.wrap(module)));
			} else {
				serviceElements.put(service, Collections.<com.google.inject.spi.Element>emptyList());
			}
		}

		for (Map.Entry<Service, List<com.google.inject.spi.Element>> entry : serviceElements.entrySet()) {
			Service service = entry.getKey();
			List<com.google.inject.spi.Element> elements = entry.getValue();

			serviceElements.put(service, ServiceModuleRewriter.annotateExposedWithId(elements, service));
		}

		List<String> hints = new ArrayList<>();

		for (Map.Entry<Service, List<com.google.inject.spi.Element>> entry : serviceElements.entrySet()) {
			Service service = entry.getKey();
			List<com.google.inject.spi.Element> elements = entry.getValue();

			serviceElements.put(service, ServiceModuleRewriter.importBindings(elements, service, serviceElements,
					hints));
		}

		for (Map.Entry<Service, List<com.google.inject.spi.Element>> entry : serviceElements.entrySet()) {
			List<com.google.inject.spi.Element> elements = entry.getValue();
			modules.add(Elements.getModule(elements));
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

		buildServiceNameMap(config.getSubServices());
		verifySingletonServicesAreUnique(config.getSubServices());
		injector = buildInjector();
	}

	@Override
	public Injector getGlobalInjector() {
		return injector;
	}

	@Override
	public void start() {
		logger.info("Starting services");
		for (Service service : config.getSubServices()) {
			if (logger.isInfoEnabled()
					&& ReflectionTools.isMethodOverriden(service.getClass(), Service.class, "start")) {
				logger.info("Starting: {}", serviceNameMap.get(service));
			}
			startedServices.add(service);
			service.start();
		}
		logger.info("All services started");
	}

	@Override
	public void stop() {
		if (config == null) {
			return;
		}

		for (Service service : startedServices) {
			String name = serviceNameMap.get(service);
			if (name == null) {
				name = service.getClass().getSimpleName();
			}
			logger.info("Stopping: {}", name);
			service.stop();
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
	public Map<Service, List<Element>> getServiceElements() {
		return serviceElements;
	}

	private void verifySingletonServicesAreUnique(List<Service> services) throws ConfigurationException {
		Set<Class<?>> set = new HashSet<>();
		for (Service service : services) {
			Class<?> klass = service.getClass();
			if (ReflectionTools.isSingleton(klass)) {
				if (set.contains(klass)) {
					throw new ConfigurationException("Service '" + klass.getCanonicalName() + "' is marked"
							+ " as singleton, but is declared more than once in configuration file");
				}
				set.add(klass);

				if (!Strings.isNullOrEmpty(service.getId())) {
					throw new ConfigurationException("Service '" + klass.getCanonicalName() + "' is marked"
							+ " as singleton, but has an id specified");
				}
			}
		}
	}

	private void buildServiceNameMap(List<Service> services) {
		for (Service service : services) {
			StringBuilder builder = new StringBuilder();

			if (service.getId() != null) {
				builder.append("\"");
				builder.append(service.getId());
				builder.append("\" ");
			}

			String summary = ClassIndex.getClassSummary(service.getClass());
			builder.append(service.getClass().getSimpleName());
			if (summary != null) {
				builder.append(" (");
				builder.append(summary);
				builder.append(")");
			}
			serviceNameMap.put(service, builder.toString());
		}
	}
}
