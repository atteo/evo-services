/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.moonshine.webdriver.browsers;

import java.io.IOException;

import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChromeBrowser implements Browser {
	private static final Logger logger = LoggerFactory.getLogger(ChromeBrowser.class);

	private ChromeDriverService service;

	public ChromeBrowser() {
		System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "target/chromedriver.log");
		try {
			service = ChromeDriverService.createDefaultService();
			service.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public RemoteWebDriver createDriver() {
		return new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
	}

	@Override
	public void close() {
		service.stop();
	}
}
