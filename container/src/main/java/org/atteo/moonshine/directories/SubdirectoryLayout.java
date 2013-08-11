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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * Subdirectory layout.
 *
 * <p>
 * Registers ${root}/config as config home and ${root}/data as data home.
 * </p>
 */
public class SubdirectoryLayout implements WriteableDirectoryLayout {
	private final Path configHome;
	private final Path dataHome;
	private final Path cacheHome;
	private final Path logHome;

	/**
	 * Creates subdirectory layout.
	 * @param root root directory of this layout
	 */
	public SubdirectoryLayout(Path root) throws IOException {
		Files.createDirectories(root);
		configHome = root.resolve("config");
		dataHome = root.resolve("data");
		cacheHome = root.resolve("cache");
		logHome = root.resolve("logs");
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
		return logHome;
	}

	@Override
	public Iterable<Path> getConfigDirs() {
		return Collections.emptyList();
	}

	@Override
	public Iterable<Path> getDataDirs() {
		return Collections.emptyList();
	}

}
