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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.webserver.WebServerService;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

@XmlRootElement(name = "webdriver-helper")
public class WebDriverHelperService extends TopLevelService {
	@ImportService
	@XmlElement
	@XmlIDREF
	private WebDriverService webDriver;

	@ImportService
	@XmlElement
	@XmlIDREF
	private WebServerService webServer;

	/**
	 * The timeout in seconds when an expectation is called.
	 */
	@XmlElement
	private int timeoutInSeconds = 2;

	/**
	 * The duration in milliseconds to sleep between polls.
	 */
	@XmlElement
	private int sleepInMillis = 10;

	@XmlElement
	@XmlDefaultValue("target/screenshots")
	private String screenshotDirectory;

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				Path path = FileSystems.getDefault().getPath(screenshotDirectory);

				try {
					Files.createDirectories(path);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}

				bind(Path.class).annotatedWith(ScreenshotDirectory.class).toInstance(path);

				bind(WebDriverHelper.class).in(Singleton.class);
				expose(WebDriverHelper.class);

				bind(WebDriverHelperOptions.class).toInstance(new WebDriverHelperOptions() {
					@Override
					public int getTimeoutInSeconds() {
						return timeoutInSeconds;
					}

					@Override
					public int getSleepInMillis() {
						return sleepInMillis;
					}
				});
			}
		};
	}
}
