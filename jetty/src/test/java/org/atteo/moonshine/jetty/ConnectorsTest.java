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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;

import org.atteo.moonshine.tests.ServicesConfiguration;
import org.atteo.moonshine.tests.ServicesTest;
import org.atteo.moonshine.webserver.WebServerAddress;
import org.atteo.moonshine.webserver.crypto.Crypto;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpTester.Request;
import org.eclipse.jetty.http.HttpTester.Response;
import org.eclipse.jetty.server.LocalConnector;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

@ServicesConfiguration("/connectors.xml")
public class ConnectorsTest extends ServicesTest {
	@Inject
	private LocalConnector localConnector;

	@Inject
	private WebServerAddress webServerAddress;

	@Inject
	@Named("ssl")
	private WebServerAddress sslServerAddress;

	@Test
	public void dummy() {
	}

	/**
	 * Tests various Jetty handlers.
	 * <p>
	 * See <a href="https://github.com/eclipse/jetty.project/tree/master/example-jetty-embedded/src/main/java/org/eclipse/jetty/embedded">this</a> link for some examples of handlers usage.
	 * </p>
	 */
	@Test
	public void testHandlers() throws Exception {
		Request request = HttpTester.newRequest();
		request.setHeader("Host", "tester");
		request.setMethod("GET");
		request.setURI("/first/");

		ByteBuffer responses = localConnector.getResponses(request.generate());

		Response response = HttpTester.parseResponse(responses);

		assertEquals("Hello World\nfirst\n", response.getContent());
	}

	@Test
	public void testConnectionDetails() {
		assertThat(webServerAddress.getPort(), is(not(0)));
		assertThat(sslServerAddress.getPort(), is(not(0)));
	}

	@Test
	public void testHttpConnection() throws IOException {
		URL url = new URL(webServerAddress.getUrl() + "/first/");
		try (InputStream stream = url.openStream()) {
			String result = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
			assertEquals("Hello World\nfirst\n", result);
		}
	}

	@Test
	public void testSslConnection() throws IOException, NoSuchAlgorithmException, KeyManagementException {
		URL url = new URL(sslServerAddress.getUrl() + "/first/");
		URLConnection connection = url.openConnection();
		HttpsURLConnection sslConnection = (HttpsURLConnection) connection;

		sslConnection.setSSLSocketFactory(Crypto.createNoValidationSSLContext().getSocketFactory());

		try (InputStream stream = sslConnection.getInputStream()) {
			String result = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
			assertEquals("Hello World\nfirst\n", result);
		}
	}
}
