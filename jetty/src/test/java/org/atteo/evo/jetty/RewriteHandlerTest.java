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
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.testing.HttpTester;
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
		HttpTester request = new HttpTester();
		request.setHeader("Host", "tester");
		request.setMethod("GET");
		HttpTester response = new HttpTester();
		String responseString;

		// test rewritePattern
		request.setURI("/alias/");
		responseString = localConnector.getResponses(request.generate());
		response.parse(responseString);
		assertEquals("hello\n", response.getContent());

		// test rewriteRegex
		request.setURI("/alias2/");
		responseString = localConnector.getResponses(request.generate());
		response.parse(responseString);
		assertEquals("hello2\n", response.getContent());

		// test redirectPattern
		request.setURI("/redirection/");
		responseString = localConnector.getResponses(request.generate());
		response.parse(responseString);
		assertEquals(HttpStatus.MOVED_TEMPORARILY_302, response.getStatus());

		// test redirectRegex
		request.setURI("/redirection2/");
		responseString = localConnector.getResponses(request.generate());
		response.parse(responseString);
		assertEquals(HttpStatus.MOVED_TEMPORARILY_302, response.getStatus());
		assertEquals("http://tester/otherurl2/", response.getHeader("Location"));

		// test virtualHost
		request.setURI("http://virtual/virtual/");
		responseString = localConnector.getResponses(request.generate());
		response.parse(responseString);
		assertEquals("hello\n", response.getContent());

		request.setURI("http://other/virtual/");
		responseString = localConnector.getResponses(request.generate());
		response.parse(responseString);
		assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
	}
}
