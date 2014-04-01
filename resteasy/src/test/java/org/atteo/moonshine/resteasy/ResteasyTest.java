/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
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
package org.atteo.moonshine.resteasy;

import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpTester.Response;
import org.eclipse.jetty.server.LocalConnector;
import static org.junit.Assert.assertEquals;
import org.junit.Test;


@MoonshineConfiguration(skipImplicit = true, fromString = ""
    + "<config>"
    + "    <servlet-container />"
    + "    <jetty>"
    + "        <connectors>"
    + "            <local/>"
    + "        </connectors>"
    + "    </jetty>"
    + "    <resteasy>"
    + "        <prefix>/rest</prefix>"
    + "        <discoverResources>true</discoverResources>"
    + "    </resteasy>"
    + "</config>")
public class ResteasyTest extends MoonshineTest {
	@Inject
	private LocalConnector localConnector;

	@Test
	public void shouldRespond() throws Exception {
		HttpTester.Request request = HttpTester.newRequest();
		request.setHeader("Host", "tester");
		request.setMethod("GET");
		request.setURI("/rest/resource");

		ByteBuffer responses = localConnector.getResponses(request.generate());

		Response response = HttpTester.parseResponse(responses);

		assertEquals("Hello World", response.getContent());
	}
}
