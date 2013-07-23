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
	private FileAccessorCommandLineParameters parameters = new FileAccessorCommandLineParameters();
	private WriteableDirectoryLayout writeableLayout;
	private List<ReadOnlyDirectoryLayout> readOnlyLayouts = new ArrayList<>();
	private List<Path> extraConfigDirs = new ArrayList<>();
	private List<Path> extraDataDirs = new ArrayList<>();

	public DefaultFileAccessor() {
	}

	@Override
	public Object getParameters() {
		return parameters;
	}

	@Override
	public void addConfigDir(String path) {
		extraConfigDirs.add(Paths.get(path));
	}

	@Override
	public void addDataDir(String path) {
		extraDataDirs.add(Paths.get(path));
	}

	@Override
	public void setWriteableLayout(WriteableDirectoryLayout writeableLayout) {
		this.writeableLayout = writeableLayout;
	}

	@Override
	public FileAccessor getFileAccessor() throws IOException {
		final Path configHome;
		final Path dataHome;
		final Path cacheHome;
		final Path logHome;
		final List<Path> configDirs = new ArrayList<>();
		final List<Path> dataDirs = new ArrayList<>();

		WriteableDirectoryLayout layout = writeableLayout;

		if (parameters.getHomeDirectory() != null) {
			layout = new SubdirectoryLayout(Paths.get(parameters.getHomeDirectory()));
		}

		if (layout == null) {
			layout = new SubdirectoryLayout((Paths.get("")));
		}

		if (parameters.getConfigHome() != null) {
			configHome = Paths.get(parameters.getConfigHome());
		} else {
			configHome = layout.getConfigHome();
		}
		if (parameters.getDataHome() != null) {
			dataHome = Paths.get(parameters.getDataHome());
		} else {
			dataHome = layout.getDataHome();
		}
		if (parameters.getCacheHome() != null) {
			cacheHome = Paths.get(parameters.getCacheHome());
		} else {
			cacheHome = layout.getCacheHome();
		}
		if (parameters.getLogHome() != null) {
			logHome = Paths.get(parameters.getLogHome());
		} else {
			logHome = layout.getLogHome();
		}

		Iterables.addAll(configDirs, layout.getConfigDirs());
		Iterables.addAll(dataDirs, layout.getDataDirs());

		Iterables.addAll(configDirs, extraConfigDirs);
		Iterables.addAll(dataDirs, extraDataDirs);
		for (ReadOnlyDirectoryLayout readOnlyLayout : readOnlyLayouts) {
			Iterables.addAll(configDirs, readOnlyLayout.getConfigDirs());
			Iterables.addAll(dataDirs, readOnlyLayout.getDataDirs());
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

			@Override
			public Properties getProperties() {
				Properties properties = new Properties();
				properties.setProperty("configHome", configHome.toAbsolutePath().toString());
				properties.setProperty("dataHome", dataHome.toAbsolutePath().toString());
				properties.setProperty("cacheHome", cacheHome.toAbsolutePath().toString());
				properties.setProperty("logHome", logHome.toAbsolutePath().toString());

				return properties;
			}
		};
	}
}
