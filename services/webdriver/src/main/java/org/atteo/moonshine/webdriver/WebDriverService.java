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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.classindex.ClassIndex;
import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.webdriver.browsers.Browser;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Starts the browser and binds the RemoteWebDriver which can be used to drive it.
 */
@XmlRootElement(name = "webdriver")
public class WebDriverService extends TopLevelService {
	private static final Iterable<Class<? extends Browser>> browsers = ClassIndex.getSubclasses(Browser.class);

	/**
	 * Browser which should be started.
	 */
	@XmlElement(name = "browser")
	@XmlDefaultValue("${oneof:${browser},chrome}")
	private String browserName;

	/**
	 * Implicit wait time in seconds.
	 * @see WebDriver.Timeouts#implicitlyWait
	 */
	@XmlElement
	private long timeoutInSeconds = 2;

	@XmlElement
	private int windowWidth = 1024;

	@XmlElement
	private int windowHeight = 768;

	private Browser browser;
	private RemoteWebDriver driver;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				browser = getSelectedBrowser();
				driver = browser.createDriver();
				driver.manage().window().setSize(new Dimension(windowWidth, windowHeight));
				driver.manage().timeouts().implicitlyWait(timeoutInSeconds, TimeUnit.SECONDS);
				bind(RemoteWebDriver.class).toInstance(driver);
			}
		};
	}

	private Browser getSelectedBrowser() {
		for (Class<? extends Browser> browserClass : browsers) {
			String name = browserClass.getSimpleName().toLowerCase(Locale.ENGLISH);
			if (name.endsWith("browser")) {
				name = name.substring(0, name.length() - "browser".length());
			}
			if (name.equals(browserName)) {
				try {
					return browserClass.newInstance();
				} catch (InstantiationException| IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		throw new RuntimeException("Selected browser not found: '" + browserName + "'");
	}

	@Override
	public void close() {
		try {
			if (driver != null) {
				driver.close();
			}
		} catch (UnreachableBrowserException e) {
			// ignore, might be already killed
		}
		if (browser != null) {
			browser.close();
		}
	}
}
