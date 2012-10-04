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

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.evo.jetty.crypto.Crypto;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * This connector uses efficient NIO buffers with a non blocking threading model.
 * Direct NIO buffers are used and threads are only allocated to connections with requests.
 * Synchronization is used to simulate blocking for the servlet API, and any unflushed content
 * at the end of request handling is written asynchronously.
 *
 * This connector is best used when there are a many connections that have idle periods.
 *
 * @see SslSelectChannelConnector
 */
@XmlRootElement(name = "sslselectchannel")
public class SslSelectChannelConnectorConfig extends AbstractConnectorConfig {
	@XmlElement
	@XmlDefaultValue("${configHome}/keystore.jks")
	private String keyStoreLocation;

	@XmlElement
	private String keyStorePassword = "secret";

	@XmlElement
	@XmlDefaultValue("default")
	private String keyAlias;

	@XmlElement
	@XmlDefaultValue("${configHome}/truststore.jks")
	private String trustStoreLocation;

	@XmlElement
	private String trustStorePassword = "secret";

	@XmlElement
	private boolean needClientAuth = false;

	@XmlElement
	private boolean wantClientAuth = false;

	@Override
	public Connector createConnector() {
		File keyStoreFile = new File(keyStoreLocation);
		if (!keyStoreFile.exists()) {
			Crypto.createSelfSignedCertificate(keyStoreFile, keyAlias, keyStorePassword);
		}
		SslContextFactory sslContextFactory = new SslContextFactory(true);
		sslContextFactory.setKeyStorePath(keyStoreLocation);
		sslContextFactory.setKeyStorePassword(keyStorePassword);
		sslContextFactory.setCertAlias(keyAlias);

		File trustStoreFile = new File(trustStoreLocation);
		if (trustStoreFile.exists()) {
			sslContextFactory.setTrustStore(trustStoreLocation);
		}
		sslContextFactory.setTrustStorePassword(trustStorePassword);
		sslContextFactory.setNeedClientAuth(needClientAuth);
		sslContextFactory.setWantClientAuth(wantClientAuth);

		return new SslSelectChannelConnector(sslContextFactory);
	}
}
