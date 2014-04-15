/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.moonshine.jersey;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.ServiceConfiguration;
import org.atteo.moonshine.jaxrs.Jaxrs;
import org.atteo.moonshine.services.ImportService;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.ProvisionException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.freemarker.FreemarkerViewProcessor;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Starts Jersey JAX-RS implementation.
 */
@XmlRootElement(name = "jersey")
@ServiceConfiguration(autoConfiguration = ""
		+ "<prefix>${oneof:${jersey.prefix},}</prefix>"
		+ "<discoverResources>true</discoverResources>")
public class Jersey extends Jaxrs {
	@XmlElement
	@XmlIDREF
	@ImportService
	private org.atteo.moonshine.webserver.ServletContainer servletContainer;

	/**
	 * Prefix under which JAX-RS resources should be registered.
	 */
	@XmlElement
	private String prefix = "";

	/**
	 * If true, returned XML documents will be formatted for human readability.
	 */
	@XmlElement
	private boolean formatOutput = false;

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				Map<String, String> params = new HashMap<>();
				params.put(ServletContainer.FEATURE_FILTER_FORWARD_ON_404, "true");
				if (formatOutput) {
					params.put(FeaturesAndProperties.FEATURE_FORMATTED, "true");
				}
				params.put(ServletContainer.PROPERTY_FILTER_CONTEXT_PATH, prefix);
				params.put(FreemarkerViewProcessor.FREEMARKER_TEMPLATES_BASE_PATH, "templates");

				bind(GuiceContainer.class);
				servletContainer.addFilter(new javax.inject.Provider<JerseyContainer>() {
					@Override
					public JerseyContainer get() {
						return new JerseyContainer();
					}
				}, params, prefix + "/*");

				registerResources(binder());
			}
		};
	}

	private class JerseyContainer extends ServletContainer {
		private static final long serialVersionUID = 1L;

		@Override
		protected void initiate(ResourceConfig config, WebApplication webapp) {
			webapp.initiate(config, new JerseyFactory(config));
		}
	}

	private class JerseyFactory implements IoCComponentProviderFactory {
		private Map<Class<?>,Provider<?>> providers = new HashMap<>();

		private JerseyFactory(ResourceConfig config) {
			for (JaxrsResource<?> jaxrsResource : getResources()) {
				config.getClasses().add(jaxrsResource.getResourceClass());
				providers.put(jaxrsResource.getResourceClass(), jaxrsResource.getProvider());
			}
			for (JaxrsResource<?> provider : getProviders()) {
				config.getClasses().add(provider.getResourceClass());
				providers.put(provider.getResourceClass(), provider.getProvider());
			}
		}

		@Override
		public IoCComponentProvider getComponentProvider(Class<?> c) {
			return getComponentProvider(null, c);
		}

		@Override
		public IoCComponentProvider getComponentProvider(ComponentContext cc, Class<?> c) {
			final Provider<?> provider = providers.get(c);
			if (provider == null) {
				return null;
			}

			return new IoCManagedComponentProvider() {
				@Override
				public Object getInstance() {
					try {
						return provider.get();
					} catch (ProvisionException e) {
						if (e.getCause() instanceof WebApplicationException) {
							throw (WebApplicationException)e.getCause();
						}
						throw e;
					}
				}

				@Override
				public Object getInjectableInstance(Object o) {
					return o;
				}

				@Override
				public ComponentScope getScope() {
					return ComponentScope.PerRequest;
				}
			};
		}
	}
}
