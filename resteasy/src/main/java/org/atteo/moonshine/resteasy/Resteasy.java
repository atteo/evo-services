/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.resteasy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;
import org.jboss.resteasy.plugins.guice.GuiceResourceFactory;
import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;
import org.jboss.resteasy.spi.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

/**
 * Starts RESTEasy JAX-RS implementation.
 *
 * Note that current implementation is based on Guice Servlet Filter and requires {@code servlet-container} to be
 * configured with {@code <registerGuiceFilter>true</registerGuiceFilter>}
 */
@XmlRootElement(name = "resteasy")
@Singleton
public class Resteasy extends TopLevelService {
	private final Logger log = LoggerFactory.getLogger(Resteasy.class.getName());

	@XmlElement
	@XmlIDREF
	@ImportService
	private org.atteo.moonshine.webserver.ServletContainer servletContainer;

	/**
	 * Prefix under which JAX-RS resources should be registered.
	 */
	@XmlElement
	@XmlDefaultValue("/")
	private String prefix;

	/**
	 * Automatically register in RESTEasy any class marked with &#064;
	 * {@link Path} or &#064;{@link Provider} annotations.
	 */
	@XmlElement
	private boolean discoverResources = true;

	@Inject
	private Injector injector;

	private final List<ResourceFactory> resourceFactories = new ArrayList<>();
	private final List<com.google.inject.Provider<?>> restProviders = new ArrayList<>();

	@Override
	public Module configure() {
		ServletModule servletModule = new ServletModule() {
			@Override
			protected void configureServlets() {
				Map<String, String> params = new HashMap<>();
				params.put("resteasy.servlet.mapping.prefix", prefix);

				bind(FilterDispatcher.class).in(Singleton.class);
				filter(prefix + "/*").through(FilterDispatcher.class, params);

				if (discoverResources) {
					for (Class<?> klass : ClassIndex.getAnnotated(Path.class)) {
						bind(klass);
						log.info("Discovered REST resource {} annotated by @Path.", klass.getName());
						final ResourceFactory resourceFactory = new GuiceResourceFactory(getProvider(klass), klass);
						resourceFactories.add(resourceFactory);
					}

					for (Class<?> klass : ClassIndex.getAnnotated(javax.ws.rs.ext.Provider.class)) {
						bind(klass);
						log.info("Discovered REST provider {} annotated by @Provider.", klass.getName());
						restProviders.add(getProvider(klass));
					}
				}
			}
		};

		return servletModule;
	}

	@Override
	public void start() {
		if (discoverResources) {
			FilterDispatcher resteasyFilter = injector.getInstance(FilterDispatcher.class);

			for (ResourceFactory resourceFactory : resourceFactories) {
				resteasyFilter.getDispatcher().getRegistry().addResourceFactory(resourceFactory);
			}

			for (com.google.inject.Provider<?> provider : restProviders) {
				resteasyFilter.getDispatcher().getProviderFactory().registerProviderInstance(provider.get());
			}
		}
	}
}
