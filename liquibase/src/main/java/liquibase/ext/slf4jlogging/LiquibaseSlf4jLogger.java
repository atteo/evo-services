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
package liquibase.ext.slf4jlogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.logging.LogLevel;

/**
 * Route Liquibase logging through Slf4j.
 *
 * <p>
 * Liquibase will automatically discover and use this logger
 * as long as it is in liquibase.ext.* package.
 * </p>
 */
public class LiquibaseSlf4jLogger implements liquibase.logging.Logger {
	private String name = "liquibase";
	private Logger logger = LoggerFactory.getLogger(name);

	public LiquibaseSlf4jLogger() {
	}

	@Override
	public int getPriority() {
		return 5;
	}

	@Override
	public void setName(String name) {
		logger = LoggerFactory.getLogger(name);
	}

	@Override
	public void setLogLevel(String logLevel, String logFile) {
		// ignore
	}

	@Override
	public void severe(String message) {
		logger.error(message);
	}

	@Override
	public void severe(String message, Throwable e) {
		logger.error(message, e);
	}

	@Override
	public void warning(String message) {
		logger.warn(message);
	}

	@Override
	public void warning(String message, Throwable e) {
		logger.warn(message, e);
	}

	@Override
	public void info(String message) {
		logger.info(message);
	}

	@Override
	public void info(String message, Throwable e) {
		logger.info(message, e);
	}

	@Override
	public void debug(String message) {
		logger.debug(message);
	}

	@Override
	public void debug(String message, Throwable e) {
		logger.debug(message, e);
	}

	@Override
	public void setLogLevel(String level) {
	}

	@Override
	public void setLogLevel(LogLevel level) {
	}

	@Override
	public LogLevel getLogLevel() {
		return null;
	}
}
