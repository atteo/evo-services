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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * A Connection Factory for SSL Connections.
 */
@XmlRootElement(name = "ssl")
public class SslConnectionFactoryConfig extends ConnectionFactoryConfig {
	@XmlIDREF
	@XmlElement(name = "sslcontextfactory", required = true)
	private SslContextFactoryConfig sslContextFactory;

	@XmlElement
	private String nextProtocol = "http/1.1";

	@Override
	public ConnectionFactory getConnectionFactory() {
		SslContextFactory factory = null;
		if (sslContextFactory != null) {
			factory = sslContextFactory.getSslContextFactory();
		}
		return new SslConnectionFactory(factory, nextProtocol);
	}

	@Override
	public String getProtocolString() {
		// TODO: this is correct assuming nextProtocol is HTTP
		return "https";
	}
}
