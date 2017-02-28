/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.resteasy;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.ServiceConfiguration;
import org.atteo.moonshine.jaxrs.Jaxrs;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.webserver.WebServerService;
import org.jboss.resteasy.plugins.guice.GuiceResourceFactory;
import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;
import org.jboss.resteasy.spi.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

/**
 * Starts RESTEasy JAXRS implementation.
 */
@XmlRootElement(name = "resteasy")
@ServiceConfiguration(autoConfiguration = ""
		+ "<prefix>${oneof:${resteasy.prefix},${jaxrs.prefix},}</prefix>"
		+ "<discoverResources>true</discoverResources>")
public class Resteasy extends Jaxrs {
    Logger log = LoggerFactory.getLogger(Resteasy.class);

	@XmlElement
	@XmlIDREF
	@ImportService
	private org.atteo.moonshine.webserver.ServletContainer servletContainer;

	@XmlElement
	@XmlIDREF
	@ImportService
	private WebServerService webServer;

	/**
	 * Prefix under which JAXRS resources should be registered.
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

				registerResources(binder());
                registerProviders(binder());
			}
		};
	}

    @Inject
    private FilterDispatcher filterDispatcher;

	@Override
	public void start() {
		for (final JaxrsResource<?> resourceWithProvider : getResources()) {
			final ResourceFactory resourceFactory = new GuiceResourceFactory(
					(com.google.inject.Provider<Object>) resourceWithProvider.getProvider()::get, resourceWithProvider.getResourceClass());
			filterDispatcher.getDispatcher().getRegistry().addResourceFactory(resourceFactory);
		}

		for (JaxrsResource<?> provider : getProviders()) {
			filterDispatcher.getDispatcher().getProviderFactory().registerProviderInstance(
					provider.getProvider().get());
		}
	}
}
