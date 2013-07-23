/*
 * Copyright 2012 Atteo.
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
package org.atteo.moonshine.services;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class ServicesCommandLineParameters {
	@Parameter(names = "--config", description = "Services configuration file(s)."
			+ " By default all config.xml files found in any directory from ${configHome}"
			+ " and ${configDirs} are merged.")
	private List<String> configurationFiles = new ArrayList<>();

	@Parameter(names = "--no-defaults", description = "Do not read default configuration")
	private boolean noDefaults;

	@Parameter(names = "--print-config", description = "Print combined configuration to standard output")
	private boolean printConfig;

	@Parameter(names = "--print-filtered-config", description = "Print combined and filtered configuration"
			+ " to standard output")
	private boolean printFilteredConfig;

	@Parameter(names = "--print-guice-bindings", description = "Print all bindings registered in Guice")
	private boolean printGuiceBindings;

	public List<String> getConfigurationFiles() {
		return configurationFiles;
	}

	public boolean isNoDefaults() {
		return noDefaults;
	}

	public boolean isPrintConfig() {
		return printConfig;
	}

	public boolean isPrintFilteredConfig() {
		return printFilteredConfig;
	}

	public boolean isPrintGuiceBindings() {
		return printGuiceBindings;
	}
}

