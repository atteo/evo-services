/*
 * Copyright 2011 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.filtering.PropertyNotFoundException;
import org.atteo.evo.filtering.PropertyResolver;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionBdd.when;

public class MoonshineTest {
	@Test
	public void shouldStartWithDefaults() throws IncorrectConfigurationException, IOException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.build()) {
			moonshine.start();
		}
	}

	@Test
	public void shouldStartWithTrivialConfiguration() throws IncorrectConfigurationException, IOException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.addConfigurationFromString(""
				+ "<config>"
				+ "</config>")
				.build()) {
			moonshine.start();
		}
	}

	@Test
	public void shouldThrowWhenSingletonWithId() throws IncorrectConfigurationException, IOException {
		// given
		try (Moonshine moonshine = when(Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.addConfigurationFromString(""
				+ "<config>"
				+ "    <singletonService id='test'/>"
				+ "</config>"))
				.build()) {

			// then
			assertThat(caughtException()).isInstanceOf(IncorrectConfigurationException.class)
					.hasMessage("Service 'org.atteo.moonshine.SingletonService' is marked as singleton, but has an id specified");
		}
	}

	@Test
	public void shouldImportBindings() throws IncorrectConfigurationException, IOException {
		// given
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addConfigurationFromResource("/robot-service.xml")
				.build()) {
			moonshine.start();

			// when
			Robot robot = moonshine.getGlobalInjector().getInstance(Robot.class);

			// then
			assertThat(robot).isNotNull();
			assertThat(robot.getLeftLeg()).isNotNull();
		}
	}

	@Test
	public void shouldUseCustomModule() throws IncorrectConfigurationException, IOException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addModule(new AbstractModule() {
					@Override
					protected void configure() {
						bind(Leg.class);
					}
				})
				.build()) {
			moonshine.start();

			// when
			Leg leg = moonshine.getGlobalInjector().getInstance(Leg.class);

			// then
			assertThat(leg).isNotNull();
		}
	}

	@Test
	public void shouldInjectMembers() throws IncorrectConfigurationException, IOException {
		// given
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addConfigurationFromString(""
				+ "<config>"
				+ "    <injectmembers>"
				+ "        <subservice/>"
				+ "    </injectmembers>"
				+ "</config>")
				.addModule(new AbstractModule() {
					@Override
					protected void configure() {
						bind(String.class).annotatedWith(Names.named("message")).toInstance("Message");
					}
				})
				.build()) {
			moonshine.start();

			// when
			String message = moonshine.getGlobalInjector().getInstance(
					Key.get(String.class, Names.named("injected message")));

			// then
			assertThat(message).isNotNull();
		}
	}

	@Test
	public void shouldInjectCustomProperty() throws IOException, IncorrectConfigurationException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addConfigurationFromString(""
				+ "<config>"
				+ "    <assertions>"
				+ "        <equals>"
				+ "            <expected>value</expected>"
				+ "            <actual>${property}</actual>"
				+ "        </equals>"
				+ "    </assertions>"
				+ "</config>")
				.addPropertyResolver(new PropertyResolver() {
					@Override
					public String resolveProperty(String property, PropertyResolver pr)
							throws PropertyNotFoundException {
						if ("property".equals(property)) {
							return "value";
						}
						return null;
					}
				})
				.build()) {

			moonshine.start();
		}
	}

	@Test
	public void shouldInjectProperties() throws IOException, IncorrectConfigurationException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addConfigurationFromString(""
				+ "<config>"
				+ "    <assertions>"
				+ "        <contains>"
				+ "            <expected>config</expected>"
				+ "            <actual>${configHome}</actual>"
				+ "        </contains>"
				+ "        <contains>"
				+ "            <expected>data</expected>"
				+ "            <actual>${dataHome}</actual>"
				+ "        </contains>"
				+ "        <contains>"
				+ "            <expected>cache</expected>"
				+ "            <actual>${cacheHome}</actual>"
				+ "        </contains>"
				+ "    </assertions>"
				+ "</config>")
				.build()) {

			moonshine.start();
		}
	}

	@Test
	public void shouldEnableInfo() throws IOException, IncorrectConfigurationException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.arguments(new String[] { "--loglevel", "INFO" })
				.build()) {

			// given
			moonshine.start();

			// when
			Logger logger = LoggerFactory.getLogger("test");

			// then
			assertThat(logger.isInfoEnabled()).isEqualTo(true);
			assertThat(logger.isDebugEnabled()).isEqualTo(false);
		}
	}

	@Test
	public void shouldEnableDebugLogging() throws IOException, IncorrectConfigurationException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.arguments(new String[] { "--loglevel", "DEBUG" })
				.build()) {

			// given
			moonshine.start();

			// when
			Logger logger = LoggerFactory.getLogger("test");

			// then
			assertThat(logger.isInfoEnabled()).isEqualTo(true);
			assertThat(logger.isDebugEnabled()).isEqualTo(true);
		}
	}

	private static class CustomParameters implements ParameterProcessor {
		@Parameter(names = "--custom")
		public Boolean custom = false;

		@Override
		public void configure(Moonshine.RestrictedBuilder builder) {
			assertThat(builder).isNotNull();
		}
	}

	@Test
	public void shouldUseCustomCommandLineObject() throws IncorrectConfigurationException, IOException {
		CustomParameters custom = new CustomParameters();
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addParameterProcessor(custom)
				.arguments(new String[] { "--custom" })
				.build()) {

			// then
			assertThat(custom.custom).isEqualTo(true);
		}
	}
}
