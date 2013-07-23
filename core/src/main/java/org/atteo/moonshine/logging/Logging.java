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

import java.util.Properties;

import org.atteo.moonshine.directories.FileAccessor;

public interface Logging {

	/**
	 * Setups some temporary logging before full configuration is available.
	 */
	void earlyBootstrap();

	/**
	 * Returns object to be filled with parameters using JCommander.
	 */
	Object getParameters();

	/**
	 * Setups final logging based on provided configuration.
	 */
	void initialize(FileAccessor fileAccessor, Properties properties);
}
