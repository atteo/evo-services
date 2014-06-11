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
package org.atteo.moonshine.jetty.connectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConnectionFactory;

/**
 * A Connection Factory for HTTP Connections.
 * <p>
 * Accepts connections either directly or via SSL and/or NPN chained connection factories.
 * </p>
 */
@XmlRootElement(name = "http")
public class HttpConnectionFactoryConfig extends ConnectionFactoryConfig {
	@Override
	public ConnectionFactory getConnectionFactory() {
		HttpConnectionFactory factory = new HttpConnectionFactory();
		return factory;
	}

	@Override
	public String getProtocolString() {
		return "http";
	}
}
