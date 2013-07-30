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

import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.moonshine.directories.FileAccessorFactory;
import org.atteo.moonshine.logging.Logback;
import org.atteo.moonshine.logging.Logging;

import com.google.inject.Injector;
import com.google.inject.Module;

import ch.qos.logback.classic.jul.LevelChangePropagator;

/**
 * Moonshine framework starting class.
 */
public interface Moonshine extends AutoCloseable {
	public interface Builder {
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
		 * Enables or disables registering shutdown hook with {@link Runtime#addShutdownHook(Thread)}.
		 */
		Builder shutdownHook(boolean shutdownHook);

		/**
		 * Sets logging framework implementation.
		 */
		Builder loggingFramework(Logging logging);

		/**
		 * Sets custom file accessor implementation.
		 */
		Builder fileAccessor(FileAccessorFactory fileAccessorFactory);

		/**
		 * Adds parameter processor.
		 *
		 * <p>
		 * Moonshine parses the parameters using {@link JCommander}. The provided object
		 * will be added to the list of parameter processors with {@link JCommander#addObject(Object)}.
		 * </p>
		 */
		Builder addParameterProcessor(Object object);

		/**
		 * Sets home directory for default file accessor.
		 * <p>
		 * If you want more control you can set your own file accessor using {@link #fileAccessor(org.atteo.moonshine.directories.FileAccessorFactory)}.
		 * </p>
		 */
		Builder homeDirectory(String homeDirectory);

		/**
		 * Adds configuration from given resource.
		 */
		Builder addConfigurationFromResource(String resource);

		/**
		 * Adds configuration from given resource, if it exists.
		 *
		 * <p>
		 * Ignores the resource, if it does not exist.
		 * </p>
		 */
		Builder addOptionalConfigurationFromResource(String config);

		/**
		 * Adds custom Guice module.
		 */
		Builder addModule(Module module);

		/**
		 * Builds Moonshine based on this builder parameters.
		 */
		Moonshine build() throws IOException;

		/**
		 * Adds configuration from given string.
		 */
		Builder addConfigurationFromString(String string);

		/**
		 * Add property resolver.
		 */
		Builder addPropertyResolver(PropertyResolver propertyResolver);

		/**
		 * Add read-only config directory.
		 */
		Builder addConfigDir(String path);

		/**
		 * Add read-only data directory.
		 */
		Builder addDataDir(String path);
	}

	public static class Factory {
		public static Builder builder() {
			return new MoonshineImplementation();
		}
	}

	/**
	 * Starts Moonshine.
	 *
	 * <p>
	 * The following operations are performed:
	 * <ul>
	 * <li>first, 'Bootstrapping Moonshine' message is logged through SLF4J, this should trigger
	 * logging framework initialization, by default Moonshine is configured to use {@link Logback}
	 * and also provides the default logback.xml file which logs WARN and ERROR messages to the console
	 * <li>shutdown hook is registered, so Moonshine will shutdown cleanly before virtual machine stops</li>
	 * <li>{@link Logging#earlyBootstrap()} is called, the default implementation for Logback redirects
	 * JUL logs through SLF4J and also initializes {@link LevelChangePropagator}</li>
	 * <li>command line parameters are parsed</li>
	 * <li>file accessor is initialized</li>
	 * <li>{@link Logging#initialize(FileAccessor, Properties)} is called, the default implementation
	 * for Logback loads logback-moonshine.xml which should load final logging configuration, by default Moonshine
	 * contains logback-moonshine.xml file which logs INFO messages to the file in ${logHome}
	 * directory</li>
	 * <li>
	 * {@link Services#start()} is called which reads configuration file and starts all configured services.
	 * </li>
	 * </ul>
	 * </p>
	 * @throws IncorrectConfigurationException when there is an error in some configuration file
	 * @throws IOException when there is some problem during file access
	 */
	void start() throws IncorrectConfigurationException, IOException;

	/**
	 * Returns the global injector of the Moonshine.
	 */
	Injector getGlobalInjector();

	/**
	 * Stops Moonshine framework.
	 */
	@Override
	void close();
}
