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
import org.atteo.filtering.PropertyFilter;
import org.atteo.filtering.PropertyNotFoundException;
import org.atteo.filtering.PropertyResolver;
import org.atteo.moonshine.services.LifeCycleListener;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionAssertJ.then;
import static com.googlecode.catchexception.apis.CatchExceptionAssertJ.when;

public class MoonshineTest {
	@Test
	public void shouldStartWithDefaults() throws MoonshineException, IOException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.build()) {
			moonshine.start();
		}
	}

	@Test
	public void shouldStartWithTrivialConfiguration() throws MoonshineException, IOException {
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
	public void shouldThrowWhenSingletonWithId() throws MoonshineException, IOException {
		// given
		try (Moonshine moonshine = when(Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.addConfigurationFromString(""
				+ "<config>"
				+ "    <singletonService id='test'/>"
				+ "</config>"))
				.build()) {

			// then
			then(caughtException()).isInstanceOf(MoonshineException.class)
					.hasMessage("Service '\"test\" SingletonService' is marked as singleton, but has an id specified");
		}
	}

	@Test
	public void shouldThrowWhenMultipleSingletonServices() throws MoonshineException, IOException {
		// given
		try (Moonshine moonshine = when(Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.addConfigurationFromString(""
				+ "<config>"
				+ "    <singletonService/>"
				+ "    <singletonService/>"
				+ "</config>"))
				.build()) {

			then(caughtException()).isInstanceOf(MoonshineException.class)
					.hasMessageContaining("Service 'SingletonService' is marked as singleton,"
					+ " but is declared more than once in configuration file");
		}
	}

	@Test
	public void shouldAllowMultipleNonSingletonServices() throws MoonshineException, IOException {
		// given
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.addConfigurationFromString(""
				+ "<config>"
				+ "    <head id='first'/>"
				+ "    <head id='second'/>"
				+ "</config>")
				.build()) {

			// then
		}
	}

	@Test
	public void shouldAllowSingletonAndNonSingletonServicesToCoexist() throws MoonshineException, IOException {
		// given
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.addConfigurationFromString(""
				+ "<config>"
				+ "    <head/>"
				+ "    <singletonService/>"
				+ "</config>")
				.build()) {

			// then
		}
	}

	@Test
	public void shouldImportService() throws MoonshineException, IOException {
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
	public void shouldUseCustomModule() throws MoonshineException, IOException {
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
	public void shouldInjectMembers() throws MoonshineException, IOException {
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
	public void shouldInjectCustomProperty() throws IOException, MoonshineException {
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
					public String resolveProperty(String property, PropertyFilter filter)
							throws PropertyNotFoundException {
						if ("property".equals(property)) {
							return "value";
						}
						throw new PropertyNotFoundException(property);
					}
				})
				.build()) {

			moonshine.start();
		}
	}

	@Test
	public void shouldInjectProperties() throws IOException, MoonshineException {
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
	public void shouldEnableInfo() throws IOException, MoonshineException {
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
	public void shouldEnableDebugLogging() throws IOException, MoonshineException {
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
	public void shouldUseCustomCommandLineObject() throws MoonshineException, IOException {
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

	@Test
	public void shouldExitAfterPrintingHelp() throws MoonshineException, IOException {
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.arguments(new String[] { "--help" })
				.build()) {

			// then
			assertThat(moonshine).isNull();
		}
	}

	@Test
	public void shouldDetectNonSingletonServicesBindingWithAnnotation() throws MoonshineException, IOException {
		try (Moonshine moonshine = when(Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addConfigurationFromString(""
					+ "<config>"
					+ "    <incorrect/>"
					+ "</config>"))
				.build()) {
		}

		then(caughtException()).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Only services marked with @Singleton can bind with annotation");
	}

	@Test
	public void shouldDetectNonSingletonServicesBindingWithAnnotationInPrivateModule()
			throws MoonshineException, IOException {
		try (Moonshine moonshine = when(Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addConfigurationFromString(""
					+ "<config>"
					+ "    <incorrect-private/>"
					+ "</config>"))
				.build()) {
		}

		then(caughtException()).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Only services marked with @Singleton can expose bindings with annotation");
	}

	@Test
	public void shouldExecuteDeconfigureEvenWhenInjectorCreationFails() throws MoonshineException, IOException {
		try (Moonshine moonshine = when(Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addConfigurationFromString(""
						+ "<config>"
						+ "    <missing-dependency-service/>"
						+ "</config>"))
				.build()) {
		}

		then(caughtException()).isInstanceOf(CreationException.class)
				.hasMessageContaining("Explicit bindings are required and java.lang.String is not explicitly bound.");

		assertThat(MissingDependencyService.configureCount).isEqualTo(1);
		assertThat(MissingDependencyService.closeCount).isEqualTo(1);
	}

	@Test
	public void shouldFireListeners() throws MoonshineException, IOException {
		LifeCycleListener listener = Mockito.mock(LifeCycleListener.class);
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home/")
				.registerListener(listener)
				.build()) {
			Mockito.verify(listener).configured(moonshine.getGlobalInjector());
			moonshine.start();
			Mockito.verify(listener).started();
		}
		Mockito.verify(listener).stopping();
		Mockito.verify(listener).closing();
	}

	@Test
	public void shouldAutoConfigure() throws MoonshineException, IOException {
		// given
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.autoConfiguration()
				.homeDirectory("target/test-home/")
				.build()) {

			// when
			Head instance = moonshine.getGlobalInjector().getInstance(Head.class);

			// then
			assertThat(instance).isNotNull();
			assertThat(instance.getName()).isEqualTo("default-name");

		}
	}

	@Test
	public void shouldDetectCyclicDependencies() throws MoonshineException, IOException {
		try (Moonshine moonshine = when(Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.addConfigurationFromString(""
					+ "<config>"
					+ "    <cyclic-service-a />"
					+ "    <cyclic-service-b />"
					+ "</config>"))
				.build()) {
		}

		// then
		then(caughtException()).isInstanceOf(RuntimeException.class)
				.hasMessage("Service CyclicServiceA depends on itself: CyclicServiceA -> CyclicServiceB -> CyclicServiceA");
	}

	@Test
	public void shouldProvidePropertyFilter() throws MoonshineException, IOException, PropertyNotFoundException {
		// given
		try (Moonshine moonshine = Moonshine.Factory.builder()
				.homeDirectory("target/test-home")
				.build()) {

			// when
			Injector injector = moonshine.getGlobalInjector();
			PropertyFilter filter = injector.getInstance(Key.get(PropertyFilter.class, ApplicationProperties.class));

			// then
			assertThat(filter).isNotNull();
			assertThat(filter.getProperty("dataHome")); // throws exception when the test fails
		}
	}
}
