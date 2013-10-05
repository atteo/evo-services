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
package org.atteo.moonshine.jolokia;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.atteo.moonshine.webserver.WebServerAddress;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <jmx/>"
		+ "    <servlet-container/>"
		+ "    <jetty>"
		+ "        <connectors>"
		+ "            <serverconnector>"
		+ "                <provideAddress>true</provideAddress>"
		+ "            </serverconnector>"
		+ "        </connectors>"
		+ "    </jetty>"
		+ "    <jolokia/>"
		+ "</config>")
public class JolokiaServiceTest extends MoonshineTest {
	@Inject
	private WebServerAddress address;

	@Test
	public void shouldReturnJmxData() throws MalformedURLException, IOException {
		// given
		URL url = new URL(address.getUrl() + "/jolokia/read/org.atteo.moonshine.jolokia:type=Car/Color");

		try (InputStream stream = url.openStream()) {
			// when
			String result = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));

			// then
			assertThat(result).contains("blue");
		}
	}
}
