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

package org.atteo.moonshine.webjars.WebJarsServiceTest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.atteo.moonshine.webserver.WebServerAddress;
import org.junit.Test;

import com.google.common.base.Strings;

@MoonshineConfiguration(autoConfiguration = true)
public class WebJarsServiceTest extends MoonshineTest {
	@Inject
	private WebServerAddress address;

	@Test
	public void shouldServetWebJar() throws MalformedURLException, IOException {
		// given
		String host = address.getHost();
		if (Strings.isNullOrEmpty(host)) {
			host = "127.0.0.1";
		}
		URL url = new URL("http", host, address.getPort(),
				"/webjars/bootstrap/3.1.0/css/bootstrap.min.css");

		// when
		Object content = url.openConnection().getContent();

		// then
		System.out.println(content);
	}
}
