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

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * This connector uses efficient NIO buffers with a non blocking threading model.
 * Direct NIO buffers are used and threads are only allocated to connections with requests.
 * Synchronization is used to simulate blocking for the servlet API, and any unflushed content
 * at the end of request handling is written asynchronously.
 *
 * This connector is best used when there are a many connections that have idle periods.
 *
 * @see ServerConnector
 */
@XmlRootElement(name = "serverconnector")
public class ServerConnectorConfig extends AbstractNetworkConnectorConfig {
	/**
	 * List of connection factories.
	 */
	@XmlElementWrapper(name = "connections")
	@XmlElementRef
	private ConnectionFactoryConfig[] connections = new ConnectionFactoryConfig[] {
		new HttpConnectionFactoryConfig()
	};

	@Override
	public AbstractNetworkConnector createConnector(Server server) {
		ConnectionFactory[] connectionFactories = new ConnectionFactory[connections.length];
		int i = 0;
		for (ConnectionFactoryConfig connectionConfig : connections) {
			connectionFactories[i] = connectionConfig.getConnectionFactory();
			i++;
		}
		return new ServerConnector(server, connectionFactories);
	}
}
