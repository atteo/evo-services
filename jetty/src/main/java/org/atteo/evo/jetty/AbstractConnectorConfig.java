/*
 * Copyright 2012 Atteo.
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
package org.atteo.evo.jetty;

import javax.xml.bind.annotation.XmlElement;

import org.eclipse.jetty.server.Connector;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

public abstract class AbstractConnectorConfig extends ConnectorConfig {
	/**
	 * The configured port for the connector or 0 if any available port may be used.
	 */
	@XmlElement
	private Integer port;

	/**
	 * The hostname representing the interface to which this connector will bind, or null for all interfaces.
	 */
	@XmlElement
	private String host;

	/**
	 * Register {@link JettyConnectionDetails} in Guice with port and hostname assigned to this connector.
	 */
	@XmlElement
	private boolean provideConnectionDetails = false;

	abstract protected Connector createConnector();

	private Connector connector;

	@Override
	public Connector getConnector() {
		connector = createConnector();
		if (port != null) {
			connector.setPort(port);
		}
		if (host != null) {
			connector.setHost(host);
		}
		return connector;
	}

	@Override
	public Module configure() {
		if (!provideConnectionDetails) {
			return null;
		}
		return new AbstractModule() {
			@Override
			protected void configure() {
				JettyConnectionDetails connectionDetails = new JettyConnectionDetails() {
					@Override
					public int getPort() {
						return connector.getLocalPort();
					}

					@Override
					public String getHost() {
						return connector.getHost();
					}
				};
				if (getId() != null) {
					bind(Key.get(JettyConnectionDetails.class, Names.named(getId()))).toInstance(connectionDetails);
				} else {
					bind(JettyConnectionDetails.class).toInstance(connectionDetails);
				}
			}
		};
	}
}
