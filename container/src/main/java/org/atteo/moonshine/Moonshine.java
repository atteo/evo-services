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
package org.atteo.moonshine;

import java.io.IOException;

import javax.annotation.Nullable;

import org.atteo.evo.config.Configuration;
import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.filtering.EnvironmentPropertyResolver;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.evo.filtering.SystemPropertyResolver;
import org.atteo.evo.filtering.XmlPropertyResolver;
import org.atteo.moonshine.directories.FileAccessor;
import org.atteo.moonshine.logging.Logback;
import org.atteo.moonshine.logging.Logging;
import org.atteo.moonshine.services.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import ch.qos.logback.classic.jul.LevelChangePropagator;

/**
 * Moonshine container starting class.
 * <h3>The following operations are performed:</h3>
 * <p>
 * <ul>
 * <li>first, 'Bootstrapping Moonshine' message is logged through SLF4J, this should trigger
 * logging framework initialization, by default Moonshine is configured to use {@link Logback}
 * and also provides the default logback.xml file which logs WARN and ERROR messages to the console</li>
 * <li>{@link Runtime#addShutdownHook(Thread) shutdown hook} is registered, so Moonshine can shutdown
 * cleanly before virtual machine stops</li>
 * <li>{@link Logging#earlyBootstrap()} is called, the default implementation for Logback redirects
 * JUL logs through SLF4J and also initializes {@link LevelChangePropagator}</li>
 * <li>command line parameters are parsed</li>
 * <li>{@link FileAccessor file accessor} is initialized</li>
 * <li>{@link Logging#initialize(FileAccessor, Properties)} is called, the default implementation
 * for Logback loads logback-moonshine.xml which should load final logging configuration, by default Moonshine
 * contains logback-moonshine.xml file which logs INFO messages to the file in ${logHome}
 * directory</li>
 * <li>configuration is loaded from a number of configuration files, by default '/default-config.xml'
 * classpath resource and ${configHome}/config.xml file, the result is {@link Config} object,</li>
 * <li>list of configured services is obtained by executing {@link Config#getSubServices()},</li>
 * <li> {@link Guice} {@link Injector injector} is created based on the modules returned from each Service's
 * {@link Service#configure()} method,</li>
 *  <li>{@link Injector#injectMembers members injection} is performed on each service,</li>
 *  <li>{@link Service#start() start} is executed on each service.</li>
 * </ul>
 * </p>
 *
 * <h3>Configuration files</h3>
 * <p>
 * Two configuration files are searched for. First the classpath is searched for '/default-config.xml'.
 * The idea is that this file contains default configuration prepared by the application programmer.
 * Next application configuration directory '${configHome} is searched for config.xml file which should
 * contain the configuration prepared by the application administrator. If config.xml file does not exist
 * it is created with the reference to the XSD schema to which the file should conform.
 * </p>
 *
 * <h3>Property resolvers</h3>
 * <p>
 * Configuration files are merged and then filtered with a number of predefined {@link PropertyResolver}s.
 * In the files @code ${name}} placeholder will be replaced in the following ways:
 * <ul>
 *   <li>all Java system properties, see {@link SystemPropertyResolver},</li>
 *   <li>environment variables can be referenced using env prefix, ex ${env.PATH},
 * see {@link EnvironmentPropertyResolver},</li>
 *   <li>all elements in the XML configuration file can be referenced using dot to separate tag names,
 * see {@link XmlPropertyResolver},</li>
 *   <li>custom properties under {@code <properties>} section in the configuration file,</li>
 *   <li>properties are resolved recursively, for instance: ${env.${VARNAME}}</li>
 *   <li>you can add your own custom {@link PropertyResolver}s using
 * {@link Moonshine.Builder#addPropertyResolver(PropertyResolver)}.</li>
 * </ul>
 * Read the description of the {@link Configuration} engine to learn more about merging, filtering
 * and validating the configuration file.
 * </p>
 *
 * </li>
 * </ul>
 * </p>
 */
public interface Moonshine extends AutoCloseable {
	public interface RestrictedBuilder {
		/**
		 * Enables or disables registering shutdown hook with {@link Runtime#addShutdownHook(Thread)}.
		 */
		RestrictedBuilder shutdownHook(boolean shutdownHook);

		/**
		 * Sets home directory for default file accessor.
		 */
		RestrictedBuilder homeDirectory(String homeDirectory);

		/**
		 * Skips default configuration files.
		 * <p>
		 * By default '/default-config.xml' classpath resource and ${configHome}/config.xml configuration
		 * files are read. This option ignores them.
		 * </p>
		 */
		RestrictedBuilder skipDefaultConfigurationFiles();

		/**
		 * Adds configuration from given resource.
		 */
		RestrictedBuilder addConfigurationFromResource(String resource);

		/**
		 * Adds configuration from given resource, if it exists.
		 *
		 * <p>
		 * Ignores the resource, if it does not exist.
		 * </p>
		 */
		RestrictedBuilder addOptionalConfigurationFromResource(String config);

		/**
		 * Adds custom Guice module.
		 */
		RestrictedBuilder addModule(Module module);

		/**
		 * Adds configuration from given string.
		 */
		RestrictedBuilder addConfigurationFromString(String string);

		/**
		 * Add property resolver.
		 */
		RestrictedBuilder addPropertyResolver(PropertyResolver propertyResolver);

		/**
		 * Add read-only config directory.
		 */
		RestrictedBuilder addConfigDir(String path);

		/**
		 * Add read-only data directory.
		 */
		RestrictedBuilder addDataDir(String path);
	}

	public interface Builder extends RestrictedBuilder {
		/**
		 * Sets application name.
		 */
		Builder applicationName(String applicationName);

		/**
		 * Sets program arguments.
		 *
		 * <p>
		 * Usually this will be the value of argv parameter from main(String[]) method.
		 * </p>
		 */
		Builder arguments(String[] arguments);

		/**
		 * Do not register uncaught exception handler.
		 */
		Builder skipUncaughtExceptionHandler();

		/**
		 * Adds parameter processor.
		 *
		 * <p>
		 * Moonshine parses the parameters using {@link JCommander}. The provided object
		 * will be added to the list of parameter processors with {@link JCommander#addObject(Object)}.
		 * </p>
		 */
		Builder addParameterProcessor(ParameterProcessor parameterProcessor);

		@Override
		Builder shutdownHook(boolean shutdownHook);

		/**
		 * Sets logging framework implementation.
		 */
		Builder loggingFramework(Logging logging);

		@Override
		Builder homeDirectory(String homeDirectory);

		@Override
		Builder skipDefaultConfigurationFiles();

		@Override
		Builder addConfigurationFromResource(String resource);

		@Override
		Builder addOptionalConfigurationFromResource(String config);

		@Override
		Builder addModule(Module module);

		@Override
		Builder addConfigurationFromString(String string);

		@Override
		Builder addPropertyResolver(PropertyResolver propertyResolver);

		@Override
		Builder addConfigDir(String path);

		@Override
		Builder addDataDir(String path);

		/**
		 * Builds Moonshine based on this builder parameters.
		 * <p>
		 * Can return null, if based on provided configuration Moonshine container
		 * is not supposed to be started. For instance, when '--help' command line parameter
		 * was specified help message should be logged and program should exit immediately.
		 * </p>
		 * @return created Moonshine container, or null if intended behavior is to skip container creation
		 * @throws IOException when configuration could not be accessed
		 * @throws IncorrectConfigurationException when configuration is incorrect
		 */
		@Nullable
		Moonshine build() throws MoonshineException, IOException;
	}

	public static class Factory {
		public static Builder builder() {
			return new MoonshineImplementation();
		}

		public static void logException(MoonshineException e) {
			Logger logger = LoggerFactory.getLogger("Moonshine");

			if (e instanceof ConfigurationException) {
				logger.error("Incorrect configuration file: " + e.getMessage());
				logger.debug("Incorrect configuration file", e);
			} else if (e instanceof CommandLineParameterException) {
				logger.error(e.getMessage());
				logger.debug(e.getMessage(), e);
			} else {
				logger.error("Fatal error: " + e.getMessage());
				logger.debug("Fatal error", e);
			}
		}
	}

	/**
	 * Starts all configured services.
	 */
	void start();

	/**
	 * Stops all configured services.
	 */
	void stop();

	/**
	 * Returns the global injector of the Moonshine.
	 * @return Moonshine global injector
	 */
	Injector getGlobalInjector();

	/**
	 * Stops Moonshine framework.
	 */
	@Override
	void close();
}
