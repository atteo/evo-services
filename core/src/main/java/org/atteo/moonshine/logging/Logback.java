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
package org.atteo.moonshine.logging;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.atteo.moonshine.directories.FileAccessor;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Logging implementation using Logback.
 *
 * <p>
 * Logging steps:
 * 1. At first logging is configured using Logback internal mechanism. It finds logback.xml on classpath
 * and reads configuration from there. We don't have yet any place for storage, so we log provide logback.xml
 * which will log WARNings and Moonshine startup messages to the console.
 * 2. During early bootstrap we disable default JUL logger which prints everything to the console
 * and we start SLF4J bridge which forwards all JUL logs to SLF4J
 * 3. Finally we load logback-moonshine.xml file which configures full logging.
 * </p>
 */
public class Logback implements Logging {
	/**
	 * Disable JUL logging and redirect all logs though SLF4J.
	 * @see <a href="http://stackoverflow.com/questions/2533227/how-can-i-disable-the-default-console-handler-while-using-the-java-logging-api>How can I disable the default console handler</a>
	 * @throws SecurityException
	 */
	protected void redirectLogsToSLF4J() {
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		for (Handler handler : rootLogger.getHandlers()) {
			rootLogger.removeHandler(handler);
		}
		SLF4JBridgeHandler.install();
	}

	protected void propagateLogbackLevelsToJul() {
		// Propagate logging levels to JUL for performance reasons, for details see:
		// http://logback.qos.ch/manual/configuration.html#LevelChangePropagator
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.addListener(new LevelChangePropagator());
	}

	protected void loadFinalLoggingConfiguration(FileAccessor fileAccessor, Properties properties) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		// the context was probably already configured by default configuration
		// rules
		context.reset();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			for (String property : properties.stringPropertyNames()) {
				context.putProperty(property, properties.getProperty(property));
			}
			Enumeration<URL> resources = this.getClass().getClassLoader().getResources("logback-moonshine.xml");
			for (; resources.hasMoreElements();) {
				URL resource = resources.nextElement();
				try (InputStream inputStream = resource.openStream()) {
					configurator.doConfigure(inputStream);
				}
			}
			for (Path path : fileAccessor.getConfigFiles("logback-moonshine.xml")) {
				try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
					configurator.doConfigure(inputStream);
				}
			}
		} catch (JoranException|IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void earlyBootstrap() {
		redirectLogsToSLF4J();
		propagateLogbackLevelsToJul();
	}

	@Override
	public Object getParameters() {
		return new LoggingCommandLineParameters();
	}

	@Override
	public void initialize(FileAccessor fileAccessor, Properties properties) {
		loadFinalLoggingConfiguration(fileAccessor, properties);
	}
}
