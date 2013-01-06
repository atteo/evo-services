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

import javax.inject.Inject;

import org.atteo.evo.tests.ServicesConfiguration;
import org.atteo.evo.tests.ServicesTest;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.testing.HttpTester;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

@ServicesConfiguration("/custom-connector.xml")
public class CustomConnectorTest extends ServicesTest {
	@Test
	public void dummy() {
	}

	@Inject
	private LocalConnector localConnector;

	@Inject
	private JettyConnectionDetails connectionDetails;

	/**
	 * Tests various Jetty handlers.
	 * <p>
	 * See <a href="https://github.com/eclipse/jetty.project/tree/master/example-jetty-embedded/src/main/java/org/eclipse/jetty/embedded">this</a> link for some examples of handlers usage.
	 * </p>
	 */
	@Test
	public void testHandlers() throws Exception {
		HttpTester request = new HttpTester();
		request.setHeader("Host", "tester");
		request.setMethod("GET");
		request.setURI("/first/");

		String responseString = localConnector.getResponses(request.generate());

		HttpTester response = new HttpTester();
		response.parse(responseString);

		assertEquals("Hello World\nfirst\n", response.getContent());
	}

	@Test
	public void testConnectionDetails() {
		assertThat(connectionDetails.getPort(), is(not(0)));
	}
}
