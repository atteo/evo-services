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
package org.atteo.evo.webdriver;

import javax.inject.Inject;

import org.atteo.evo.webserver.WebServerAddress;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

/**
 * WebDriver helper functions.
 */
public class WebDriverHelper {
	@Inject
	private WebServerAddress webServerAddress;

	@Inject
	private RemoteWebDriver driver;

	@Inject
	private WebDriverHelperOptions options;

	/**
	 * Loads the specified path from websever;
	 */
	public void go(String path) {
		String host = (webServerAddress.getHost() == null) ? "localhost" : webServerAddress.getHost();
		String address = "http://" + host + ":" + webServerAddress.getPort() + path;
		driver.get(address);
	}

	public <T> void waitUntil(Function<WebDriver, T> function) {
		waitUntil(function, null);
	}

	public <T> void waitUntil(Function<WebDriver, T> function, String message) {
		WebDriverWait wait = new WebDriverWait(driver, options.getTimeoutInSeconds(), options.getSleepInMillis());

		if (message != null) {
			wait.withMessage(message);
		}

		wait.until(function);
	}

	public void waitUntilPath(final String path) {
		waitUntil(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return driver.getCurrentUrl().matches(".*" + path);
			}
		}, "Path did not change to '" + path + "'");
	}
}
