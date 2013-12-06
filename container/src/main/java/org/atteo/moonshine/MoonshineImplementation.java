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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;
import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.filtering.PropertiesPropertyResolver;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.moonshine.directories.DefaultFileAccessor;
import org.atteo.moonshine.directories.FileAccessor;
import org.atteo.moonshine.directories.FileAccessorFactory;
import org.atteo.moonshine.directories.SubdirectoryLayout;
import org.atteo.moonshine.logging.Logback;
import org.atteo.moonshine.logging.Logging;
import org.atteo.moonshine.services.Services;
import org.atteo.moonshine.services.internal.GuiceBindingsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

class MoonshineImplementation implements Moonshine.Builder, Moonshine {
	private final Thread shutdownThread = new Thread() {
		@Override
		public void run() {
			close();
		}
	};

	private static class MoonshineUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (e instanceof MoonshineException) {
				Moonshine.Factory.logException((MoonshineException) e);
			} else {
				Logger logger = LoggerFactory.getLogger("Moonshine");
				logger.error("Fatal error: " + e.getMessage(), e);
			}
		}
	}

	private final Logger logger = LoggerFactory.getLogger("Moonshine");
	private Services services;
	private Logging logging;
	private FileAccessorFactory fileAccessorFactory;
	private String applicationName;
	private String[] arguments;
	private String homeDirectory;
	private final List<ParameterProcessor> parameterProcessors = new ArrayList<>();
	private final List<String> configurationResources = new ArrayList<>();
	private final List<String> optionalConfigurationResources = new ArrayList<>();
	private final List<String> configurationStrings = new ArrayList<>();
	private final List<Module> modules = new ArrayList<>();
	private final List<PropertyResolver> propertyResolvers = new ArrayList<>();
	private final List<String> configDirs = new ArrayList<>();
	private final List<String> dataDirs = new ArrayList<>();
	private boolean shutdownHook = true;
	private boolean skipDefaultConfigurationFile = false;
	private boolean skipExceptionHandler = false;

	@Override
	public Builder applicationName(String applicationName) {
		this.applicationName = applicationName;
		return this;
	}

	@Override
	public Builder skipUncaughtExceptionHandler() {
		skipExceptionHandler = true;
		return this;
	}

	@Override
	public Builder arguments(String[] arguments) {
		this.arguments = arguments;
		return this;
	}

	@Override
	public Builder shutdownHook(boolean shutdownHook) {
		this.shutdownHook = shutdownHook;
		return this;
	}

	@Override
	public Builder loggingFramework(Logging logging) {
		this.logging = logging;
		return this;
	}

	@Override
	public Builder addParameterProcessor(ParameterProcessor processor) {
		this.parameterProcessors.add(processor);
		return this;
	}

	@Override
	public Moonshine build() throws IOException, CommandLineParameterException, ConfigurationException {
		logger.info("Bootstrapping {}", applicationName != null ? applicationName : "Moonshine");

		if (!skipExceptionHandler) {
			Thread.currentThread().setUncaughtExceptionHandler(new MoonshineUncaughtExceptionHandler());
		}

		if (logging == null) {
			logging = new Logback();
		}
		logging.earlyBootstrap();

		fileAccessorFactory = new DefaultFileAccessor();

		JCommander commander = new JCommander();
		MoonshineCommandLineParameters moonshineParameters = new MoonshineCommandLineParameters();
		commander.setProgramName(applicationName);
		commander.addObject(moonshineParameters);
		commander.addObject(logging.getParameters());
		commander.addObject(fileAccessorFactory.getParameters());
		for (Object object : parameterProcessors) {
			commander.addObject(object);
		}
		if (arguments == null) {
			arguments = new String[]{};
		}
		try {
			commander.parse(arguments);
		} catch (ParameterException e) {
			throw new CommandLineParameterException("Cannot parse command line parameters: " + e.getMessage(), e);
		}

		for (ParameterProcessor parameterProcessor : parameterProcessors) {
			parameterProcessor.configure(this);
		}

		Path homePath;
		if (homeDirectory == null) {
			homePath = Paths.get("");
		} else {
			homePath = Paths.get(homeDirectory);
		}
		fileAccessorFactory.setWriteableLayout(new SubdirectoryLayout(homePath));

		for (String configDir : configDirs) {
			fileAccessorFactory.addConfigDir(configDir);
		}
		for (String dataDir : dataDirs) {
			fileAccessorFactory.addDataDir(dataDir);
		}
		if (applicationName == null) {
			applicationName = "moonshine";
		}
		if (moonshineParameters.isHelp()) {
			StringBuilder builder = new StringBuilder();
			commander.usage(builder);
			logger.info(builder.toString());
			return null;
		}

		FileAccessor fileAccessor = fileAccessorFactory.getFileAccessor();
		Properties fileAccessorProperties = fileAccessor.getProperties();
		fileAccessorProperties.setProperty("applicationName", applicationName);
		logging.initialize(fileAccessor, fileAccessorProperties);

		final ConfigurationReader configuration = new ConfigurationReader(fileAccessor);
		Config config;
		try {
			setupConfiguration(moonshineParameters, configuration);
			configuration.addCustomPropertyResolver(new PropertiesPropertyResolver(fileAccessorProperties));

			if (moonshineParameters.isPrintConfig()) {
				logger.info("Configuration is:\n" + configuration.printCombinedXml());
				return null;
			}

			configuration.filter();

			if (moonshineParameters.isPrintFilteredConfig()) {
				logger.info("Filtered configuration is:\n" + configuration.printCombinedXml());
				return null;
			}

			config = configuration.read();
		} catch (IncorrectConfigurationException e) {
			throw new ConfigurationException(e.getMessage(), e);
		}

		final PropertyResolver propertyResolver = configuration.getPropertyResolver();

		modules.add(new AbstractModule() {
			@Override
			protected void configure() {
				bind(Key.get(PropertyResolver.class, ApplicationProperties.class)).toInstance(propertyResolver);
			}
		});

		Services.Builder builder = createServicesBuilder();
		for (Module module : modules) {
			builder.addModule(module);
		}
		builder.configuration(config);

		services = builder.build();

		if (moonshineParameters.isPrintGuiceBindings()) {
			GuiceBindingsHelper.printServiceElements(services.getServiceElements());
		}
		if (shutdownHook) {
			Runtime.getRuntime().addShutdownHook(shutdownThread);
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
	public Builder skipDefaultConfigurationFiles() {
		skipDefaultConfigurationFile = true;
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
    public Builder registerAppModules() {
        for (Class<?> moduleClass : ClassIndex.getAnnotated(AppModule.class)) {
            if (Module.class.isAssignableFrom(moduleClass)) {
                logger.trace("Found @AppModule [{}].", moduleClass.getName());
                try {
                    Module module = (Module)moduleClass.newInstance();
                    modules.add(module);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not instantiate AppModule {}" + moduleClass.getName(), e);
                }
            } else {
                throw new IllegalStateException("Class " + moduleClass.getName()
                        + " is annotated as @AppModule but doesn't implement com.google.inject.Module");
            }
        }

        return this;
    }

    @Override
	public Builder addPropertyResolver(PropertyResolver propertyResolver) {
		propertyResolvers.add(propertyResolver);
		return this;
	}

	protected Services.Builder createServicesBuilder() {
		return Services.Factory.builder();
	}

	@Override
	public void start() {
		services.start();
	}

	@Override
	public void stop() {
		services.stop();
	}

	@Override
	public void close() {
		logger.info("Shutting down {}", applicationName != null ? applicationName : "Moonshine");
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownThread);
		} catch (IllegalStateException e) {
			// ok, will be thrown if we are already in the process of shutting down JVM
		}
		if (services != null) {
			services.close();
			services = null;
		}
	}

	@Override
	public Injector getGlobalInjector() {
		return services.getGlobalInjector();
	}

	protected void setupConfiguration(MoonshineCommandLineParameters moonshineParameters,
			final ConfigurationReader configuration) throws IOException, IncorrectConfigurationException {

		if (!skipDefaultConfigurationFile && !moonshineParameters.isNoDefaults()) {
			configuration.combineDefaultConfiguration();
		}
		for (String resource : configurationResources) {
			configuration.combineConfigurationFromResource(resource, true);
		}
		for (String resource : optionalConfigurationResources) {
			configuration.combineConfigurationFromResource(resource, false);
		}
		for (String string : configurationStrings) {
			configuration.combineConfigurationFromString(string);
		}
		if (moonshineParameters.getConfigurationFiles().isEmpty()) {
			configuration.combineConfigDirConfiguration();
		} else {
			for (String fileName : moonshineParameters.getConfigurationFiles()) {
				configuration.combineConfigurationFromFile(new File(fileName), true);
			}
		}
		configuration.generateTemplateConfigurationFile();
		for (PropertyResolver resolver : propertyResolvers) {
			configuration.addCustomPropertyResolver(resolver);
		}
	}
}
