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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.jetty.Jetty;
import org.atteo.evo.services.ImportBindings;
import org.atteo.evo.services.TopLevelService;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

@XmlRootElement(name = "webdriver-helper")
public class WebDriverHelperService extends TopLevelService {
	@ImportBindings
	@XmlElement
	@XmlIDREF
	private WebDriverService webDriver;

	@ImportBindings
	@XmlElement
	@XmlIDREF
	private Jetty jettyService;

	/**
	 * The timeout in seconds when an expectation is called.
	 */
	@XmlElement
	private int timeoutInSeconds;

	/**
	 * The duration in milliseconds to sleep between polls.
	 */
	@XmlElement
	private int sleepInMillis;

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(WebDriverHelper.class);
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
