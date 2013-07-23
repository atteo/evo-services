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
package org.atteo.moonshine.directories;

import java.nio.file.Path;
import java.util.Properties;

public interface FileAccessor {
	/**
	 * Returns the first existing config file found with given name.
	 * @return path to the config file, or null if not exist
	 */
	Path getConfigFile(String fileName);

	/**
	 * Return all existing config files with given name.
	 */
	Iterable<Path> getConfigFiles(String fileName);

	/**
	 * Return the path where config file with given name should exist.
	 * The file may or may not exist.
	 */
	Path getWritebleConfigFile(String fileName);

	/**
	 * Returns the first existing data file found with given name.
	 * @return path to the data file, or null if not exist
	 */
	Path getDataFile(String fileName);

	/**
	 * Returns all existing data files with given name.
	 */
	Iterable<Path> getDataFiles(String fileName);

	/**
	 * Returns the path where data file with given name should stored.
	 * The file may or may not exist.
	 */
	Path getWriteableDataFile(String fileName);

	/**
	 * Returns the path where cache file with given name should stored.
	 * The file may or may not exist.
	 */
	Path getWriteableCacheFile(String fileName);

	/**
	 * Returns the path where log files with given name should be stored.
	 * The file may or may not exist.
	 */
	Path getWriteableLogfile(String fileName);

	/**
	 * Returns filtering properties.
	 */
	Properties getProperties();
}
