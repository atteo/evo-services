/*
 * Copyright 2014 Atteo.
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

package org.atteo.moonshine.jaxrs;

import javax.inject.Inject;

import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineConfiguration.Config;
import org.atteo.moonshine.tests.MoonshineTest;
import org.atteo.moonshine.webserver.WebServerAddress;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <servlet-container/>"
		+ "</config>",
		forEach = {
			@Config(id = "jetty", fromString = ""
					+ "<config>"
					+ "    <jetty/>"
					+ "</config>"),
			@Config(id = "tomcat", fromString = ""
					+ "<config>"
					+ "    <tomcat/>"
					+ "</config>")})
public abstract class JaxrsTest extends MoonshineTest {
	@Inject
	private WebServerAddress address;

	private final String prefix;

	public JaxrsTest() {
		prefix = "/rest";
	}

	public JaxrsTest(String prefix) {
		this.prefix = prefix;
	}

	@Test
	public void shouldRespondToHelloWorld() throws Exception {
		// given
		RestAssured.given().port(address.getPort())

		// when
				.when().get(prefix + "/hello")

		//then
				.then().content(equalTo("Hello World"));
	}
}
