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

import com.beust.jcommander.Parameter;

public class LoggingCommandLineParameters {
	public static enum LogLevel {
		TRACE, DEBUG, INFO, WARN, ERROR, FATAL
	}

	@Parameter(names = "--loglevel", description = "Sets logging level")
	private LogLevel logLevel;

	public LogLevel getLogLevel() {
		return logLevel;
	}
}
