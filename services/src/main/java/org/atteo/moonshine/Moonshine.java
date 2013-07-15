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
import org.atteo.moonshine.logging.Logging;

import com.google.inject.Injector;
import com.google.inject.Module;

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
		 * Set program arguments.
		 *
		 * <p>
		 * Usually this will be the value of argv parameter from main(String[]) method.
		 * </p>
		 */
		Builder setArguments(String[] arguments);

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
		Builder addPropertyResolved(PropertyResolver propertyResolver);

		/**
		 * Add read-only config directory.
		 */
		Builder addConfigDir(String path);

		/**
		 * Add read-only data directory.
		 */
		Builder addDataDir(String path);
	}

	/**
	 * Start Moonshine framework and all configured services.
	 * @throws IncorrectConfigurationException
	 * @throws IOException
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
