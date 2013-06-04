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
package org.atteo.evo.webserver;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.annotation.XmlElement;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Provides address to the remote web server.
 * <p>
 * Does not start any web server, just binds {@link WebServerAddress}
 * with the address provided in the configuration.
 * </p>
 */
public class RemoteWebServerService extends WebServerService {
	/**
	 * Address to the remote web server.
	 * <p>
	 * For instance: "http://127.0.0.1:8080/"
	 * </p>
	 */
	@XmlElement(required = true)
	private String url;

	private int port;
	private String host;

	@Override
	public Module configure() {
		try {
			URI uri = new URI(url);
			port = uri.getPort();
			host = uri.getHost();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(WebServerAddress.class).toInstance(new WebServerAddress() {
					@Override
					public int getPort() {
						return port;
					}

					@Override
					public String getHost() {
						return host;
					}

					@Override
					public String getUrl() {
						return url;
					}
				});
			}
		};
	}
}
