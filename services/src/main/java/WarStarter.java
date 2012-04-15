/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Starts the application from the WAR file.
 * 
 * First we need to unzip the war file. Then we need to setup the classloader
 * which will load the classes from unzipped directory. This is equivalent to
 * the functionality offered when war file is started by the (Jetty) container.
 *
 * <p>
 * One important thing here is that the order of class loading needs to be changed.
 * The default for Java is to load the class first from parent class loader
 * and only then from this cloassloader. Here we need to reverse that order
 * as specified by Servlet specification.
 * </p>
 *
 * <p>
 * To use the functionality offered by this class add the following XML to POM of the WAR file:
 *
 * <pre>{@code
 *		<plugin>
 *			<artifactId>maven-dependency-plugin</artifactId>
 *			<executions>
 *				<execution>
 *					<goals>
 *						<goal>unpack</goal>
 *					</goals>
 *					<phase>prepare-package</phase>
 *					<configuration>
 *						<artifactItems>
 *							<artifactItem>
 *								<groupId>org.atteo</groupId>
 *								<artifactId>evo-services</artifactId>
 *								<version>1.0</version>
 *							</artifactItem>
 *						</artifactItems>
 *						<includes>WarStarter.class</includes>
 *						<outputDirectory>${project.build.directory}/${project.build.finalName}</outputDirectory>
 *					</configuration>
 *				</execution>
 *			</executions>
 *		</plugin>
 }</pre>
 * </p>
 */
public class WarStarter {
	private static final int BUFFER_SIZE = 4096;

	private static void removeDirectory(File directory) {
		if (!directory.exists() || !directory.isDirectory()) {
			return;
		}
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				removeDirectory(file);
			} else {
				if (!file.delete()) {
					throw new RuntimeException("Cannot delete file: " + file.getAbsolutePath());
				}
			}
		}
		if (!directory.delete()) {
			throw new RuntimeException("Cannot delete directory: " + directory.getAbsolutePath());
		}
	}

	private static void unzip(URL zipFile, File outputDirectory) throws FileNotFoundException,
			IOException {
		BufferedOutputStream dest;
		InputStream fis = zipFile.openStream();
		ZipInputStream zip = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry = zip.getNextEntry();
		while(entry != null) {
			//System.out.println("Extracting: " +entry);
			File file = new File(outputDirectory, entry.getName());

			if (entry.isDirectory()) {
				if (!file.exists() && !file.mkdirs()) {
					throw new RuntimeException("Cannot create directory: "
							+ file.getAbsolutePath());
				}
				entry = zip.getNextEntry();
				continue;
			}
			
			if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
				throw new IOException("Cannot create directory: " + file.getParent());
			}

			int count;
			byte data[] = new byte[BUFFER_SIZE];
			// write the files to the disk
			FileOutputStream fos = new FileOutputStream(file);
			dest = new BufferedOutputStream(fos, BUFFER_SIZE);
			count = zip.read(data, 0 , BUFFER_SIZE);
			while (count != -1 ) {
				dest.write(data, 0, count);

				count = zip.read(data, 0, BUFFER_SIZE);
			}
			dest.flush();
			dest.close();

			entry = zip.getNextEntry();
		}
		zip.close();
	}

	/**
	 * Parent last class loader.
	 *
	 * Class loader which reverses the classic order of class loading from parent class loader first.
	 * It tries to load the classes from system classloader, then from the list of provided url's
	 * and finally from parent class loader.
	 */
	private static class ParentLastClassLoader extends URLClassLoader {
		public ParentLastClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		@Override
		protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			Class<?> klass;

			klass = findLoadedClass(name);
			if (klass != null) {
				return klass;
			}

			try {
				klass = getSystemClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				klass = null;
			}
			if (klass != null) {
				return klass;
			}

			try {
				klass = findClass(name);
			} catch (ClassNotFoundException e) {
				klass = getParent().loadClass(name);
			}

			if (resolve) {
				resolveClass(klass);
			}

			return klass;
		}

	}

	/**
	 * Start entry routine.
	 *
	 * <ul>
	 * 	<li>Extract WAR file</li>
	 *  <li>Setup class loader for the extracted files</li>
	 *  <li>Start Services.start()</li>
	 * </ul>
	 *
	 * Following arguments are recognized:
	 *
	 * <ul>
	 *  <li> <b>--root</b> - set the root directory for the application, the default is "~/.appconfig"</li>
	 *  <li> <b>--expaned</b> - specifies where content of the extracted WAR file resides,
	 * this skips extracting the WAR file and class loader setup
	 *  <li> any other argument is forwarded to the Services.start() routine.
	 * </ul>
	 *
	 * @param args arguments, see description above
	 * @throws Exception any exception from Services.start()
	 */
	public static void main(String[] args) throws Exception {
		File rootDirectory = new File(".config");
		File expanded = null;
		int i =0;
		for (; i < args.length; i++) {
			if ("--root".equals(args[i])) {
				i++;
				rootDirectory = new File(args[i]);
			} else if ("--expanded".equals(args[i])) {
				i++;
				expanded = new File(args[i]);
			}
		}
		ClassLoader loader;
		if (expanded == null) {
			expanded = new File(rootDirectory, "war");

			System.out.println("Unpacking the application...");
			URL warLocation = WarStarter.class.getProtectionDomain().getCodeSource().getLocation();

			removeDirectory(expanded);
			if (!expanded.mkdirs()) {
				throw new IOException("Cannot create directory: " + expanded.getAbsolutePath());
			}

			unzip(warLocation, expanded);

			List<URL> urls = new ArrayList<URL>();
			urls.add(new URL("file://" + expanded.getAbsolutePath()
					+ "/WEB-INF/classes/"));

			File libs = new File(expanded + File.separator + "WEB-INF"
					+ File.separator + "/lib/");
			for (File lib : libs.listFiles()) {
				urls.add(new URL("jar:file://" + lib.getAbsolutePath() + "!/"));
			}

			loader = new ParentLastClassLoader(urls.toArray(new URL[] {}),
					Thread.currentThread().getContextClassLoader());
			Thread.currentThread().setContextClassLoader(loader);
		}
		System.out.println("Starting...");
		loader = Thread.currentThread().getContextClassLoader();

		Class<?> klass = loader.loadClass("org.atteo.evo.services.Services");
		Constructor<?> constructor = klass.getConstructor(File.class, File.class);
		Object instance = constructor.newInstance(rootDirectory, expanded);
		Method method = klass.getMethod("start");
		try {
			method.invoke(instance);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}
	}
}

