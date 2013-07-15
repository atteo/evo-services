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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.filtering.PropertiesPropertyResolver;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.moonshine.Moonshine.Builder;
import org.atteo.moonshine.directories.DefaultFileAccessor;
import org.atteo.moonshine.directories.FileAccessor;
import org.atteo.moonshine.directories.FileAccessorFactory;
import org.atteo.moonshine.directories.SubdirectoryLayout;
import org.atteo.moonshine.logging.Logback;
import org.atteo.moonshine.logging.Logging;
import org.atteo.moonshine.services.Services;
import org.atteo.moonshine.services.ServicesCommandLineParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.google.inject.Injector;
import com.google.inject.Module;

public class MoonshineFactory {

	public static Builder builder() {
		return new MoonshineImplementation();
	}

	private static class MoonshineImplementation implements Builder, Moonshine {
		private Thread shutdownThread = new Thread() {
			@Override
			public void run() {
				close();
			}
		};
		private Logger logger = LoggerFactory.getLogger("Moonshine");
		private Services services;
		private Logging logging;
		private FileAccessorFactory fileAccessorFactory;
		private String applicationName;
		private String[] arguments;
		private String homeDirectory;
		private List<Object> parameterProcessors = new ArrayList<>();
		private List<String> configurationResources = new ArrayList<>();
		private List<String> optionalConfigurationResources = new ArrayList<>();
		private List<String> configurationStrings = new ArrayList<>();
		private List<Module> modules = new ArrayList<>();
		private List<PropertyResolver> propertyResolvers = new ArrayList<>();
		private List<String> configDirs = new ArrayList<>();
		private List<String> dataDirs = new ArrayList<>();

		/**
		 * Sets application name.
		 */
		@Override
		public Builder applicationName(String applicationName) {
			this.applicationName = applicationName;
			return this;
		}

		@Override
		public Builder setArguments(String[] arguments) {
			this.arguments = arguments;
			return this;
		}

		/**
		 * Sets logging framework implementation.
		 */
		@Override
		public Builder loggingFramework(Logging logging) {
			this.logging = logging;
			return this;
		}

		/**
		 * Sets file accessor implementation.
		 */
		@Override
		public Builder fileAccessor(FileAccessorFactory fileAccessorFactory) {
			this.fileAccessorFactory = fileAccessorFactory;
			return this;
		}

		/**
		 * Adds parameter processor.
		 *
		 * <p>
		 * Moonshine parses the parameters using {@link JCommander}. The provided object
		 * will be added to the list of parameter processors with {@link JCommander#addObject(Object)}.
		 * </p>
		 */
		@Override
		public Builder addParameterProcessor(Object object) {
			this.parameterProcessors.add(object);
			return this;
		}

		/**
		 * Builds Moonshine based on this builder parameters.
		 */
		@Override
		public Moonshine build() throws IOException {
			if (logging == null) {
				logging = new Logback();
			}
			if (fileAccessorFactory == null) {
				Path homePath;
				if (homeDirectory == null) {
					homePath = Paths.get("");
				} else {
					homePath = Paths.get(homeDirectory);
				}
				fileAccessorFactory = new DefaultFileAccessor(new SubdirectoryLayout(homePath));
			}
			for (String configDir : configDirs) {
				fileAccessorFactory.addConfigDir(configDir);
			}
			for (String dataDir : dataDirs) {
				fileAccessorFactory.addDataDir(dataDir);
			}
			if (applicationName == null) {
				applicationName = "moonshine";
			}
			if (arguments == null) {
				arguments = new String[] {};
			}
			return this;
		}

		@Override
		public Builder homeDirectory(String homeDirectory) {
			this.homeDirectory = homeDirectory;
			return this;
		}

		@Override
		public Builder addConfigDir(String path) {
			dataDirs.add(path);
			return this;
		}

		@Override
		public Builder addDataDir(String path) {
			configDirs.add(path);
			return this;
		}

		@Override
		public Builder addConfigurationFromResource(String resource) {
			configurationResources.add(resource);
			return this;
		}

		@Override
		public Builder addOptionalConfigurationFromResource(String resource) {
			optionalConfigurationResources.add(resource);
			return this;
		}

		@Override
		public Builder addConfigurationFromString(String string) {
			configurationStrings.add(string);
			return this;
		}

		@Override
		public Builder addModule(Module module) {
			modules.add(module);
			return this;
		}

		@Override
		public Builder addPropertyResolved(PropertyResolver propertyResolver) {
			propertyResolvers.add(propertyResolver);
			return this;
		}

		protected void createServices(FileAccessor fileAccessor) {
			services = new Services(applicationName, fileAccessor);
		}

		/**
		 *
		 * start minimal console logging
		 * parse command line parameters
		 * start directories
		 * change to file logging
		 * prepare property resolvers
		 * read configuration file
		 * start services
		 */
		@Override
		public void start() throws IncorrectConfigurationException, IOException {
			logger.info("Bootstrapping Moonshine");

			logging.earlyBootstrap();

			Runtime.getRuntime().addShutdownHook(shutdownThread);

			ServicesCommandLineParameters servicesParameters = new ServicesCommandLineParameters();

			JCommander commander = new JCommander();
			commander.addObject(fileAccessorFactory.getParameters());
			commander.addObject(servicesParameters);
			commander.addObject(logging.getParameters());
			for (Object object : parameterProcessors) {
				commander.addObject(object);
			}
			commander.parse(arguments);

			FileAccessor fileAccessor = fileAccessorFactory.getFileAccessor();
			Properties fileAccessorProperties = fileAccessorFactory.getProperties();

			logging.initialize(fileAccessor, fileAccessorProperties);

			createServices(fileAccessor);
			services.setup(servicesParameters);
			for (String resource : configurationResources) {
				services.combineConfigurationFromResource(resource, true);
			}
			for (String resource : optionalConfigurationResources) {
				services.combineConfigurationFromResource(resource, false);
			}
			for (String string : configurationStrings) {
				services.combineConfigurationFromString(string);
			}
			for (Module module : modules) {
				services.addModule(module);
			}
			for (PropertyResolver resolver : propertyResolvers) {
				services.addCustomPropertyResolver(resolver);
			}
			services.addCustomPropertyResolver(new PropertiesPropertyResolver(fileAccessorProperties));
			services.start();
		}

		@Override
		public void close() {
			logger.info("Shutting down Moonshine");
			Runtime.getRuntime().removeShutdownHook(shutdownThread);

			if (services != null) {
				services.stop();
				services = null;
			}
		}

		@Override
		public Injector getGlobalInjector() {
			return services.getGlobalInjector();
		}
	}
}
