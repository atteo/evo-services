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

import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.atteo.moonshine.tests.ServicesConfiguration;
import org.atteo.moonshine.tests.ServicesTest;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpTester.Request;
import org.eclipse.jetty.http.HttpTester.Response;
import org.eclipse.jetty.server.LocalConnector;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

@ServicesConfiguration("/rewrites.xml")
public class RewriteHandlerTest extends ServicesTest {
	@Test
	public void dummy() {
	}

	@Inject
	private LocalConnector localConnector;

	@Test
	public void testRewrites() throws Exception {
		Request request = HttpTester.newRequest();
		request.setHeader("Host", "tester");
		request.setMethod("GET");

		// test rewritePattern
		request.setURI("/alias/");
		ByteBuffer responseString = localConnector.getResponses(request.generate());
		Response response = HttpTester.parseResponse(responseString);
		assertEquals("hello\n", response.getContent());

		// test rewriteRegex
		request.setURI("/alias2/");
		responseString = localConnector.getResponses(request.generate());
		response = HttpTester.parseResponse(responseString);
		assertEquals("hello2\n", response.getContent());

		// test redirectPattern
		request.setURI("/redirection/");
		responseString = localConnector.getResponses(request.generate());
		response = HttpTester.parseResponse(responseString);
		assertEquals(HttpStatus.MOVED_TEMPORARILY_302, response.getStatus());

		// test redirectRegex
		request.setURI("/redirection2/");
		responseString = localConnector.getResponses(request.generate());
		response = HttpTester.parseResponse(responseString);
		assertEquals(HttpStatus.MOVED_TEMPORARILY_302, response.getStatus());
		assertEquals("http://tester/otherurl2/", response.get(HttpHeader.LOCATION));

		// test virtualHost
		request = HttpTester.newRequest();
		request.setMethod("GET");
		request.setURI("http://virtual/virtual/");
		responseString = localConnector.getResponses(request.generate());
		response = HttpTester.parseResponse(responseString);
		assertEquals("hello\n", response.getContent());

		request.setURI("http://other/virtual/");
		responseString = localConnector.getResponses(request.generate());
		response = HttpTester.parseResponse(responseString);
		assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
	}
}
