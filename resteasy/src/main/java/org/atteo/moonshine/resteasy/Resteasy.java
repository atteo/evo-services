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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;
import org.jboss.resteasy.plugins.guice.GuiceResourceFactory;
import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;
import org.jboss.resteasy.spi.ResourceFactory;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Starts RESTEasy JAX-RS implementation.
 *
 * Note that current implementation is based on Guice Servlet Filter and
 * requires {@code servlet-container} to be configured with
 * {@code <registerGuiceFilter>true</registerGuiceFilter>}
 */
@XmlRootElement(name = "resteasy")
@Singleton
public class Resteasy extends TopLevelService {

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

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				Map<String, String> params = new HashMap<>();
				params.put("resteasy.servlet.mapping.prefix", prefix);

				bind(FilterDispatcher.class).in(Singleton.class);
				servletContainer.addFilter(getProvider(FilterDispatcher.class), params, prefix + "/*");

			}
		};
	}

	@Inject
	private FilterDispatcher filterDispatcher;

	private final List<ResourceFactory> resourceFactories = new ArrayList<>();

	private final List<com.google.inject.Provider<?>> restProviders = new ArrayList<>();

	public <T> void addResource(Provider<T> provider, Class<T> klass) {
		final ResourceFactory resourceFactory = new GuiceResourceFactory(provider, klass);
		resourceFactories.add(resourceFactory);
	}

	public <T> void addProvider(Provider<T> provider) {
		restProviders.add(provider);
	}

	List<ResourceFactory> getResourceFactories() {
		return resourceFactories;
	}

	List<Provider<?>> getRestProviders() {
		return restProviders;
	}

	@Override
	public void start() {
		for (ResourceFactory resourceFactory : resourceFactories) {
			filterDispatcher.getDispatcher().getRegistry().addResourceFactory(resourceFactory);
		}

		for (com.google.inject.Provider<?> provider : restProviders) {
			filterDispatcher.getDispatcher().getProviderFactory().registerProviderInstance(provider.get());
		}
	}
}
