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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifies directory layout based on XDG Base Directory Specification.
 * @see <a href="http://standards.freedesktop.org/basedir-spec/basedir-spec-latest.html">XDG Base Directory Specification</a>
 */
public class XdgDirectoryLayout implements WriteableDirectoryLayout {
	private final Path homeDirectory;
	private final Path configHome;
	private final Path dataHome;
	private final Path cacheHome;
	private final List<Path> configDirs = new ArrayList<>();
	private final List<Path> dataDirs = new ArrayList<>();

	public XdgDirectoryLayout(String applicationName) {
		homeDirectory = Paths.get(System.getProperty("user.home"));

		String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
		if (xdgConfigHome != null) {
			configHome = Paths.get(xdgConfigHome, applicationName);
		} else {
			configHome = homeDirectory.resolve(".config").resolve(applicationName);
		}

		String xdgDataHome = System.getenv("XDG_DATA_HOME");
		if (xdgDataHome != null) {
			dataHome = Paths.get(xdgDataHome, applicationName);
		} else {
			dataHome = homeDirectory.resolve(".local/share").resolve(applicationName);
		}

		String xdgCacheHome = System.getenv("XDG_CACHE_HOME");
		if (xdgCacheHome != null) {
			cacheHome = Paths.get(xdgCacheHome, applicationName);
		} else {
			cacheHome = homeDirectory.resolve(".cache").resolve(applicationName);
		}

		String xdgConfigDirs = System.getenv("XDG_CONFIG_DIRS");
		if (xdgConfigDirs == null) {
			xdgConfigDirs = "/etc/xdg";
		}
		for (String dir : xdgConfigDirs.split(":")) {
			configDirs.add(Paths.get(dir));
		}
		String xdgDataDirs = System.getenv("XDG_CONFIG_DIRS");
		if (xdgDataDirs == null) {
			xdgDataDirs = "/usr/local/share/:/usr/share/";
		}
		for (String dir : xdgDataDirs.split(":")) {
			dataDirs.add(Paths.get(dir));
		}
	}

	@Override
	public Path getConfigHome() {
		return configHome;
	}

	@Override
	public Path getDataHome() {
		return dataHome;
	}

	@Override
	public Path getCacheHome() {
		return cacheHome;
	}

	@Override
	public Path getLogHome() {
		return dataHome.resolve("logs");
	}

	@Override
	public Iterable<Path> getConfigDirs() {
		return configDirs;
	}

	@Override
	public Iterable<Path> getDataDirs() {
		return dataDirs;
	}
}
