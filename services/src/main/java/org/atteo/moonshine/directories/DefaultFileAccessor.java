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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Iterables;

/**
 * Manages directory structure of the application.
 */
public class DefaultFileAccessor implements FileAccessorFactory {
	private DirectoriesCommandLineParameters parameters = new DirectoriesCommandLineParameters();
	private Path configHome;
	private Path dataHome;
	private Path cacheHome;
	private Path logHome;
	private final List<Path> configDirs = new ArrayList<>();
	private final List<Path> dataDirs = new ArrayList<>();

	public DefaultFileAccessor(WriteableDirectoryLayout writeableLayout, ReadOnlyDirectoryLayout... layouts) {
		Iterables.addAll(configDirs, writeableLayout.getConfigDirs());
		Iterables.addAll(dataDirs, writeableLayout.getDataDirs());

		for (ReadOnlyDirectoryLayout layout : layouts) {
			Iterables.addAll(configDirs, layout.getConfigDirs());
			Iterables.addAll(dataDirs, layout.getDataDirs());
		}

		configHome = writeableLayout.getConfigHome();
		dataHome = writeableLayout.getDataHome();
		cacheHome = writeableLayout.getCacheHome();
		logHome = writeableLayout.getLogHome();
	}

	@Override
	public Object getParameters() {
		return parameters;
	}

	@Override
	public void addConfigDir(String path) {
		configDirs.add(Paths.get(path));
	}

	@Override
	public void addDataDir(String path) {
		dataDirs.add(Paths.get(path));
	}

	@Override
	public FileAccessor getFileAccessor() throws IOException {
		if (parameters.getConfigHome() != null) {
			configHome = Paths.get(parameters.getConfigHome());
		}
		if (parameters.getDataHome() != null) {
			dataHome = Paths.get(parameters.getDataHome());
		}
		if (parameters.getCacheHome() != null) {
			cacheHome = Paths.get(parameters.getCacheHome());
		}
		if (parameters.getLogHome() != null) {
			logHome = Paths.get(parameters.getLogHome());
		}

		Files.createDirectories(configHome);
		Files.createDirectories(dataHome);
		Files.createDirectories(cacheHome);
		Files.createDirectories(logHome);

		return new FileAccessor() {
			private Path getFile(Path home, Iterable<Path> dirs, String fileName) {
				Path filePath = home.resolve(fileName);
				if (Files.exists(filePath)) {
					return filePath;
				}

				for (Path path : dirs) {
					filePath = path.resolve(fileName);
					if (Files.exists(filePath)) {
						return filePath;
					}
				}
				return null;
			}

			private Iterable<Path> getFiles(Path home, Iterable<Path> dirs, String fileName) {
				List<Path> files = new ArrayList<>();
				Path filePath = home.resolve(fileName);
				if (Files.exists(filePath)) {
					files.add(filePath);
				}
				for (Path path : dirs) {
					filePath = path.resolve(fileName);
					if (Files.exists(filePath)) {
						files.add(filePath);
					}
				}
				return files;
			}

			@Override
			public Path getConfigFile(String fileName) {
				return getFile(configHome, configDirs, fileName);
			}

			@Override
			public Iterable<Path> getConfigFiles(String fileName) {
				return getFiles(configHome, configDirs, fileName);
			}

			@Override
			public Path getWritebleConfigFile(String fileName) {
				return configHome.resolve(fileName);
			}

			@Override
			public Path getDataFile(String fileName) {
				return getFile(dataHome, dataDirs, fileName);
			}

			@Override
			public Iterable<Path> getDataFiles(String fileName) {
				return getFiles(configHome, configDirs, fileName);
			}

			@Override
			public Path getWriteableDataFile(String fileName) {
				return dataHome.resolve(fileName);
			}

			@Override
			public Path getWriteableCacheFile(String fileName) {
				return cacheHome.resolve(fileName);
			}

			@Override
			public Path getWriteableLogfile(String fileName) {
				return logHome.resolve(fileName);
			}
		};
	}

	@Override
	public Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("configHome", configHome.toAbsolutePath().toString());
		properties.setProperty("dataHome", dataHome.toAbsolutePath().toString());
		properties.setProperty("cacheHome", cacheHome.toAbsolutePath().toString());
		properties.setProperty("logHome", logHome.toAbsolutePath().toString());

		return properties;
	}



}
