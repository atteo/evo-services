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
package org.atteo.moonshine.webdriver;

import javax.inject.Inject;

import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.openqa.selenium.remote.RemoteWebDriver;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <jetty>"
		+ "        <connectors>"
		+ "            <serverconnector>"
		+ "                <provideAddress>true</provideAddress>"
		+ "            </serverconnector>"
		+ "        </connectors>"
		+ "        <contextCollection>"
		+ "            <context>"
		+ "                <contextPath>/hello</contextPath>"
		+ "                <hello/>"
		+ "            </context>"
		+ "        </contextCollection>"
		+ "    </jetty>"
		+ "    <webdriver/>"
		+ "    <webdriver-helper/>"
		+ "</config>")
public class WebDriverTest extends MoonshineTest {
	@Inject
	private RemoteWebDriver driver;

	@Inject
	private WebDriverHelper helper;

	@Test
	public void test() {
		helper.go("/hello");

		assertThat(driver.getCurrentUrl(), endsWith("/hello/"));
		assertThat(driver.getPageSource(), containsString("Hello World"));
	}
}
