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

package org.atteo.moonshine.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.webserver.ServletContainer;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

/**
 * WebSocket container.
 */
public abstract class WebSocketContainerService extends TopLevelService {
	@ImportService
	@XmlIDREF
	@XmlElement
	protected ServletContainer servletContainer;

	private final List<EndpointDefinition<?>> endpoints = new ArrayList<>();

	protected <T> EndpointDefinition<T> createEndpointDefinition(Class<T> klass) {
		return new EndpointDefinition<>(klass);
	}

	/**
	 * Adds ordinary endpoint.
	 */
	public <T extends Endpoint> EndpointBuilder<T> addEndpoint(Class<T> klass) {
		EndpointDefinition<T> definition = createEndpointDefinition(klass);
		endpoints.add(definition);
		return definition;
	}

	/**
	 * Adds annotated endpoint.
	 * @param endpoint
	 */
	public void addAnnotatedEndpoint(Class<?> endpoint) {
		EndpointDefinition<?> definition = createEndpointDefinition(endpoint);
		endpoints.add(definition);
	}

	/**
	 * Adds annotated endpoint.
	 * @param endpoint
	 */
	public <T> void addAnnotatedEndpoint(Class<T> endpoint, Provider<? extends T> provider) {
		addAnnotatedEndpointInternal(endpoint, provider);
	}

	private <T> void addAnnotatedEndpointInternal(Class<T> endpoint, Provider<? extends T> provider) {
		@SuppressWarnings("unchecked")
		EndpointDefinition<T> definition = createEndpointDefinition(endpoint);
		definition.provider(provider);
		endpoints.add(definition);
	}

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				servletContainer.addListener(new Listener());
			}
		};
	}

	public static interface EndpointBuilder<T> {
		EndpointBuilder<T> pattern(String pattern);
		EndpointBuilder<T> provider(Provider<? extends T> provider);
		EndpointBuilder<T> addEncoder(Class<? extends Encoder> encoder);
		EndpointBuilder<T> addDecoder(Class<? extends Decoder> encoder);
		EndpointBuilder<T> addUserProperty(String key, Object value);
	}

	private class Listener implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent contextEvent) {
			ServletContext context = contextEvent.getServletContext();
			ServerContainer container = (ServerContainer) context.getAttribute(ServerContainer.class.getName());
			for (EndpointDefinition<?> endpointDefinition : endpoints) {
				try {
					container.addEndpoint(endpointDefinition);
				} catch (DeploymentException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
		}
	}

	public static class EndpointDefinition<T> implements ServerEndpointConfig, EndpointBuilder<T> {
		private final Class<T> endpointClass;
		private Provider<? extends T> provider;
		private String pattern;
		private final List<Class<? extends Encoder>> encoders = new ArrayList<>();
		private final List<Class<? extends Decoder>> decoders = new ArrayList<>();
		protected final Map<String, Object> userProperties = new HashMap<>();

		public EndpointDefinition(Class<T> endpointClass) {
			this.endpointClass = endpointClass;
		}

		@Override
		public EndpointBuilder<T> pattern(String pattern) {
			this.pattern = pattern;
			return this;
		}

		@Override
		public EndpointBuilder<T> provider(Provider<? extends T> provider) {
			this.provider = provider;
			return this;
		}

		@Override
		public EndpointBuilder<T> addEncoder(Class<? extends Encoder> encoder) {
			encoders.add(encoder);
			return this;
		}

		@Override
		public EndpointBuilder<T> addDecoder(Class<? extends Decoder> decoder) {
			decoders.add(decoder);
			return this;
		}

		@Override
		public EndpointBuilder<T> addUserProperty(String key, Object value) {
			userProperties.put(key, value);
			return this;
		}

		@Override
		public Class<T> getEndpointClass() {
			return endpointClass;
		}

		@Override
		public String getPath() {
			return pattern;
		}

		@Override
		public List<String> getSubprotocols() {
			return Collections.emptyList();
		}

		@Override
		public List<Extension> getExtensions() {
			return Collections.emptyList();
		}

		@Override
		public ServerEndpointConfig.Configurator getConfigurator() {
			return new Configurator() {
				@Override
				public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
					if (provider == null) {
						return super.getEndpointInstance(endpointClass);
					}
					return endpointClass.cast(provider.get());
				}
			};
		}

		@Override
		public List<Class<? extends Encoder>> getEncoders() {
			return encoders;
		}

		@Override
		public List<Class<? extends Decoder>> getDecoders() {
			return decoders;
		}

		@Override
		public Map<String, Object> getUserProperties() {
			return userProperties;
		}
	}
}
