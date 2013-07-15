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

import com.beust.jcommander.Parameter;

public class DirectoriesCommandLineParameters {
	@Parameter(names = "--home", description = "Base home directory")
	private String homeDirectory;

	@Parameter(names = "--data-home", description = "Directory to which user specific"
			+ " data files should be stored")
	private String dataHome;

	@Parameter(names = "--config-home", description = "Directory to which user specific"
			+ " configuration files should be stored")
	private String configHome;

	@Parameter(names = "--cache-home", description = "Directory with non-essential"
			+ " user-specific (cache) data files")
	private String cacheHome;

	@Parameter(names = "--log-home", description = "Directory with log files")
	private String logHome;

	@Parameter(names = "--print-dirs", description = "Print directories used by the app (data home, config home etc)")
	private boolean printDirectories;

	public String getHomeDirectory() {
		return homeDirectory;
	}

	public String getDataHome() {
		return dataHome;
	}

	public String getConfigHome() {
		return configHome;
	}

	public String getCacheHome() {
		return cacheHome;
	}

	String getLogHome() {
		return logHome;
	}

	public boolean isPrintDirectories() {
		return printDirectories;
	}
}
