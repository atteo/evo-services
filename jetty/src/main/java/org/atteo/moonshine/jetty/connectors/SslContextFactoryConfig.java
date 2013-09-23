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

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.Configurable;
import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.webserver.crypto.Crypto;
import org.eclipse.jetty.util.ssl.SslContextFactory;

@XmlRootElement(name = "sslcontextfactory")
public class SslContextFactoryConfig extends Configurable {
	@XmlElement
	@XmlDefaultValue("${configHome}/keystore.jks")
	private String keyStorePath;

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

	public SslContextFactory getSslContextFactory() {
		SslContextFactory factory = new SslContextFactory(true);

		File keyStoreFile = new File(keyStorePath);
		if (!keyStoreFile.exists()) {
			Crypto.createSelfSignedCertificate(keyStoreFile, keyAlias, keyStorePassword);
		}
		factory.setKeyStorePath(keyStorePath);
		factory.setKeyStorePassword(keyStorePassword);
		factory.setCertAlias(keyAlias);

		File trustStoreFile = new File(trustStoreLocation);
		if (trustStoreFile.exists()) {
			factory.setTrustStorePath(trustStoreLocation);
		}
		factory.setTrustStorePassword(trustStorePassword);
		factory.setNeedClientAuth(needClientAuth);
		factory.setWantClientAuth(wantClientAuth);

		return factory;
	}

}
