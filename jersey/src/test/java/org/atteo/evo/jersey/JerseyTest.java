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
package org.atteo.evo.jersey;

import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.atteo.evo.tests.ServicesTest;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpTester.Response;
import org.eclipse.jetty.server.LocalConnector;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class JerseyTest extends ServicesTest {
	@Inject
	private LocalConnector localConnector;

	private void request(String url) throws Exception {
		HttpTester.Request request = HttpTester.newRequest();
		request.setHeader("Host", "tester");
		request.setMethod("GET");
		request.setURI(url);

		ByteBuffer responses = localConnector.getResponses(request.generate());

		Response response = HttpTester.parseResponse(responses);

		assertEquals("Hello World", response.getContent());
	}

	@Test
	public void test() throws Exception {
		request("/rest/resource");
	}

}
