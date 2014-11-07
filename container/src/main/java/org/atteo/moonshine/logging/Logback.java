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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.atteo.moonshine.directories.FileAccessor;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.selector.ContextSelector;
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
	private final LoggingCommandLineParameters parameters = new LoggingCommandLineParameters();

	public static class MoonshineContextSelector implements ContextSelector {
		private static final InheritableThreadLocal<LoggerContext> context =
				new InheritableThreadLocal<LoggerContext>() {

			@Override
			protected LoggerContext initialValue() {
				LoggerContext context = new LoggerContext();
				context.setName(Thread.currentThread().getName());
				return context;
			}

		};

		public MoonshineContextSelector(LoggerContext c) {
			context.set(c);
		}

		/**
		 * Starts new Logback context for this thread and all threads created from this thread.
		 */
		public static void initNewContext() {
			context.remove();
		}

		@Override
		public LoggerContext getLoggerContext() {
			return context.get();
		}

		@Override
		public LoggerContext getLoggerContext(String name) {
			LoggerContext c = context.get();
			if (c.getName().equals(name)) {
				return c;
			} else {
				return null;
			}
		}

		@Override
		public LoggerContext getDefaultLoggerContext() {
			return context.get();
		}

		@Override
		public LoggerContext detachLoggerContext(String loggerContextName) {
			return context.get();
		}

		@Override
		public List<String> getContextNames() {
			return Arrays.asList(context.get().getName());
		}
	}

	static synchronized protected void newLogbackContextForThisThread() {
		if (!MoonshineContextSelector.class.getName().equals(
				System.getProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR))) {

			System.setProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR, MoonshineContextSelector.class.getName());
			try {
				Method method = StaticLoggerBinder.class.getDeclaredMethod("reset");
				method.setAccessible(true);
				method.invoke(null);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LoggerFactory.getLogger("Moonshine").warn(
						"Unrecognized Logback version, cannot install context selector", e);
			}
		}
		MoonshineContextSelector.initNewContext();

		LoggerFactory.getILoggerFactory();
	}

	/**
	 * Disable JUL logging and redirect all logs though SLF4J.
	 *
	 * @see <a href="http://stackoverflow.com/questions/2533227/how-can-i-disable-the-default-console-handler-while-using-the-java-logging-api>How can I disable the default console handler</a>
	 * @throws SecurityException
	 */
	protected void redirectLogsToSLF4J() {
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");

		boolean found = false;
		for (Handler handler : rootLogger.getHandlers()) {
			if (handler instanceof SLF4JBridgeHandler) {
				found = true;
			} else {
				rootLogger.removeHandler(handler);
			}
		}
		if (!found) {
			// skipped if another Moonshine instance already registered the bridge
			SLF4JBridgeHandler.install();
		}
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
			if (parameters.getLogLevel() != null) {
				context.putProperty("log.level", parameters.getLogLevel().name());
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
		newLogbackContextForThisThread();

		redirectLogsToSLF4J();
		propagateLogbackLevelsToJul();
	}

	@Override
	public Object getParameters() {
		return parameters;
	}

	@Override
	public void initialize(FileAccessor fileAccessor, Properties properties) {
		loadFinalLoggingConfiguration(fileAccessor, properties);
	}
}
