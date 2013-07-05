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
package org.atteo.moonshine.jetty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;

/**
 * Internal connector to the Jetty which sends data directly
 * without any network operations.
 * <p>
 * Use one of {@link LocalConnector#getResponses} methods to send/receive data.
 * {@link LocalConnector} will be available in Guice injector.
 * </p>
 */
@XmlRootElement(name = "local")
public class LocalConnectorConfig extends ConnectorConfig {
	/*
	 * Set the maximum Idle time for a connection in ms.
	 */
	@XmlElement
	private int maxIdleTime = 200000;

	private LocalConnector connector;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(LocalConnector.class).toProvider(new Provider<LocalConnector>() {
					@Override
					public LocalConnector get() {
						return connector;
					}
				});
			}
		};
	}

	@Override
	public Connector getConnector(Server server) {
		connector = new LocalConnector(server);
		connector.setIdleTimeout(maxIdleTime);
		return connector;
	}
}
