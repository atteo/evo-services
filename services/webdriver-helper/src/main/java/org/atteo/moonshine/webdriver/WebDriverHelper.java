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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;

import org.atteo.moonshine.webserver.WebServerAddress;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

/**
 * WebDriver helper functions.
 */
public class WebDriverHelper {

	private final WebServerAddress webServerAddress;

	private final RemoteWebDriver driver;

	private final WebDriverHelperOptions options;

	private Path screenshotDirectory;

	@Inject
	public WebDriverHelper(WebServerAddress webServerAddress, RemoteWebDriver driver,
	    WebDriverHelperOptions options, @ScreenshotDirectory Path screenshotDirectory) {
		this.webServerAddress = webServerAddress;
		this.driver = driver;
		this.options = options;
		this.screenshotDirectory = screenshotDirectory;
	}

	public Path getScreenshotDirectory() {
		return screenshotDirectory;
	}

	public void setScreenshotDirectory(Path screenshotDirectory) {
		this.screenshotDirectory = screenshotDirectory;
	}

	/**
	 * Loads the specified path from webserver;
	 */
	public void go(String path) {
		String host = (webServerAddress.getHost() == null) ? "localhost" : webServerAddress.getHost();
		String address = "http://" + host + ":" + webServerAddress.getPort() + path;
		driver.get(address);
	}

	public <T> T waitUntil(Function<WebDriver, T> function) {
		return waitUntil(function, null);
	}

	public <T> T waitUntil(final Function<WebDriver, T> function, String message) {
		WebDriverWait wait = new WebDriverWait(driver, options.getTimeoutInSeconds(), options.getSleepInMillis());

		if (message != null) {
			wait.withMessage(message);
		}

		return wait.until(new Function<WebDriver, T>() {

			@Override
			public T apply(WebDriver input) {
				try {
					return function.apply(input);
				} catch (StaleElementReferenceException e) {
					// Every now and then elements may be destroyed during the evaluation of 'function'
					// and this exception might be thrown. It's safe to ignore here as 'function' will be
					// called multiple times as long as it does not return true.
					return null;
				}
			}
		});
	}

	public void waitUntilPath(final String path) {
		waitUntil(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return driver.getCurrentUrl().matches(".*" + path + "/?");
			}
		}, "Path did not change to '" + path + "'");
	}

	/**
	 * Tries to click an element. If necessary waits until the element
	 * becomes clickable.
	 *
	 * Use it for elements which temporarily might not be clickable for some
	 * reason (either disabled, hidden or having other elements on top of
	 * them).
	 *
	 * @param elementLocator
	 */
	public void waitUntilClickable(final By elementLocator) {
		waitUntil(new Function<WebDriver, Boolean>() {

			@Override
			public Boolean apply(WebDriver input) {
				WebElement element = input.findElement(elementLocator);

				if (element != null && element.isDisplayed() && element.isEnabled()) {
					try {
						element.click();
						return true;
					} catch (WebDriverException e) {
					}
				}

				return false;
			}
		}, "Element is not clickable: " + elementLocator.toString());
	}

	public void screenshot(String name) throws IOException {
		File file = driver.getScreenshotAs(OutputType.FILE);
		Files.createDirectories(screenshotDirectory);
		Files.copy(file.toPath(), screenshotDirectory.resolve(name + ".png"));
	}
}
