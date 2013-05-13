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
package org.atteo.evo.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.Configuration;
import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.config.XmlUtils;
import org.atteo.evo.filtering.CompoundPropertyResolver;
import org.atteo.evo.filtering.EnvironmentPropertyResolver;
import org.atteo.evo.filtering.PropertiesPropertyResolver;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.evo.filtering.SystemPropertyResolver;
import org.atteo.evo.filtering.XmlPropertyResolver;
import org.atteo.evo.injection.InjectMembersModule;
import org.atteo.evo.services.internal.DuplicateDetectionWrapper;
import org.atteo.evo.services.internal.GuiceBindingsHelper;
import org.atteo.evo.services.internal.ServiceModuleRewriter;
import org.atteo.evo.urlhandlers.UrlHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.Elements;

/**
 * Evo Services is a runtime service engine based on Google Guice
 * which discovers services, configures them and manages their lifecycle.
 *
 * <p>
 * The engine performs the following actions when started:
 * <ul>
 *  <li>redirects all logging through {@link Logger SLF4J} with LogBack as default implementation,</li>
 *  <li>reads services configuration files config.xml and default-config.xml
 * using {@link Configuration} engine with {@link Config}
 * as a root class and a number of default property resolvers,</li>
 *  <li>collects the list of all configured services by executing {@link Config#getServices()},</li>
 *  <li>creates {@link Guice} {@link Injector injector} based on the modules returned from each Service's
 * {@link Service#configure()} method,</li>
 *  <li>performs {@link Injector#injectMembers members injection} on each service,</li>
 *  <li>executes {@link Service#start() start} on each service.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The use of SLF4J is mandatory. By default Evo Services has dependency on LogBack in POM file
 * which suffices to use it as a default logging implementation. Use
 * <a href="http://maven.apache.org/guides/introduction/introduction-to-optional-and-excludes-dependencies.html#Dependency_Exclusions">exclusions</a>
 * if you want to use different SLF4J implementation. If you stick with LogBack there is LogBack
 * service which offers further integration with the framework.
 * </p>
 *
 * <p>
 * Two configuration files are searched for. First the classpath is searched for default-config.xml.
 * The idea is that this file contains default configuration prepared by the application programmer.
 * Next application home directory is searched for config.xml file which contains the configuration
 * prepared by the application administrator. If config.xml file does not exist it is created
 * with the reference to the XSD schema to which the file should conform.
 * </p>
 *
 * <p>
 * Configuration files are merged and filtered with a number of predefined {@link PropertyResolver}s.
 * The value for {@code ${name}} placeholder will be searched in the following ways:
 * <ul>
 *   <li>${applicationHome} represents the home directory of the application,</li>
 *   <li>all Java system properties, see {@link SystemPropertyResolver},</li>
 *   <li>environment variables can be referenced using env prefix, ex ${env.PATH},
 * see {@link EnvironmentPropertyResolver},</li>
 *   <li>all elements in the XML configuration file can be referenced using dot to separate tag names,
 * see {@link XmlPropertyResolver},</li>
 *   <li>custom properties under {@code <properties>} section in the configuration file,</li>
 *   <li>properties can be recursive, for instance: ${env.${VARNAME}},
 * see {@link RecursivePropertyResolver},</li>
 *   <li>you can add your own custom {@link PropertyResolver}s using
 * {@link #addCustomPropertyResolver(PropertyResolver)}.</li>
 * </ul>
 * Read the description of the {@link Configuration} engine to learn more about merging, filtering
 * and validating the configuration file.
 * </p>
 *
 * <p>
 * The {@link Services Services engine} can be started either directly using {@link #start()} method
 * or by registering it as a listener in Servlet Container.
 * </p>
 * <p>
 * Each of the Services should extend {@link Service}. This interface defines
 * {@link Service#configure() configure()} method which allows to register Guice {@link Module},
 * {@link Service#start() start()} and {@link Service#stop() stop()} methods to execute some logic
 * upon start and stop of the application. If you want your Service to be directly
 * under the root directory in the configuration file it should extend {@link TopLevelService}.
 * Additionally each service must define the name of the tag under which it can be referenced
 * in the configuration file using {@literal @}{@link XmlRootElement} annotation.
 * </p>
 */
public class Services extends GuiceServletContextListener {
	public final String SCHEMA_FILE_NAME = "schema.xsd";
	public final String CONFIG_FILE_NAME = "config.xml";
	public final String DEFAULT_CONFIG_RESOURCE_NAME = "/default-config.xml";
	private Logger logger = LoggerFactory.getLogger("Evo Services");
	private final String applicationName;
	private File homeDirectory;
	private File dataHome;
	private File configHome;
	private File cacheHome;
	private File runtimeDirectory;
	private File dataDir;
	private List<File> configDirs;
	private List<Module> extraModules = new ArrayList<>();
	private CompoundPropertyResolver customPropertyResolvers = new CompoundPropertyResolver();

	private Configuration configuration;
	private Injector injector;
	private boolean externalContainer = false;
	private boolean printGuiceBindings = false;
	private Config config;
	private PropertyResolver propertyResolver;
	private List<Service> startedServices = new ArrayList<>();

	public Services() {
		this("test");
	}

	public Services(String applicationName) {
		this.applicationName = applicationName;
		// Redirect messages from JUL through SLF4J
		SLF4JBridgeHandler.install();
		configuration = new Configuration();

		homeDirectory = new File(System.getProperty("user.home"));

		UrlHandlers.registerAnnotatedHandlers();
	}

	private static void createDirectory(File directory) {
		if (directory == null) {
			return;
		}
		directory = directory.getAbsoluteFile(); // exists() fails for "" (current dir)
		if (!directory.exists() && !directory.mkdirs()) {
			throw new RuntimeException("Cannot create directory: "
					+ directory.getAbsolutePath());
		}
	}

	public void setHomeDirectory(File homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	public File getHomeDirectory() {
		if (homeDirectory != null) {
			return homeDirectory;
		}
		return new File(System.getProperty("user.home"));
	}

	public File getDataHome() {
		if (dataHome != null) {
			return dataHome;
		}
		String xdgDataHome = System.getenv("XDG_DATA_HOME");
		if (xdgDataHome != null) {
			return new File(xdgDataHome + "/" + applicationName);
		}

		return new File(homeDirectory, ".local/share/" + applicationName);
	}

	public File getConfigHome() {
		if (configHome != null) {
			return configHome;
		}
		String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
		if (xdgConfigHome != null) {
			return new File(xdgConfigHome + "/" + applicationName);
		}
		return new File(homeDirectory, ".config/" + applicationName);
	}

	public File getDataDir() {
		if (dataDir != null) {
			return dataDir;
		}
		return new File("./");
	}

	public List<File> getConfigDirs() {
		if (configDirs != null) {
			return configDirs;
		}
		// TODO: XDG_CONFIG_DIRS
		return Collections.emptyList();
	}

	public File getCacheHome() {
		if (cacheHome != null) {
			return cacheHome;
		}
		String xdgCacheHome = System.getenv("XDG_CACHE_HOME");
		if (xdgCacheHome != null) {
			return new File(xdgCacheHome + "/" + applicationName);
		}
		return new File(homeDirectory, ".cache/" + applicationName);
	}

	public File getRuntimeDirectory() {
		if (runtimeDirectory != null) {
			return runtimeDirectory;
		}
		String xdgRuntimeDir = System.getenv("XDG_RUNTIME_DIR");
		if (xdgRuntimeDir != null) {
			return new File(xdgRuntimeDir);
		}
		return new File(homeDirectory, ".run");
	}

	public void setDataHome(File dataHome) {
		this.dataHome = dataHome;
	}

	public void setConfigHome(File configHome) {
		this.configHome = configHome;
	}

	public void setCacheHome(File cacheHome) {
		this.cacheHome = cacheHome;
	}

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public void setConfigDirs(List<File> configDirs) {
		this.configDirs = configDirs;
	}

	public void setRuntimeDirectory(File runtimeDirectory) {
		this.runtimeDirectory = runtimeDirectory;
	}

	/**
	 * Reads configuration from '/default-config.xml' resource.
	 */
	public void combineDefaultConfiguration() {
		try {
			combineConfigurationFromResource(DEFAULT_CONFIG_RESOURCE_NAME, false);
		} catch (IOException | IncorrectConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads configuration from config.xml files found in ${configDirs} and ${configHome} directories.
	 */
	public void combineConfigDirConfiguration() throws IncorrectConfigurationException, IOException {
		for (File configDir : getConfigDirs()) {
			combineConfigurationFromFile(new File(configDir, CONFIG_FILE_NAME), false);
		}
		combineConfigurationFromFile(new File(getConfigHome(), CONFIG_FILE_NAME), false);
	}

	/**
	 * Reads configuration from given resource.
	 * @param resourcePath path to the resource
	 * @throws IncorrectConfigurationException when configuration is incorrect
	 * @throws IOException when cannot read resource
	 */
	public void combineConfigurationFromResource(String resourcePath, boolean throwIfNotFound)
			throws IncorrectConfigurationException, IOException {
		// TODO: what if more than one resource with given name?
		try(InputStream stream = getClass().getResourceAsStream(resourcePath)) {
			if (stream != null) {
				configuration.combine(stream);
			} else if (throwIfNotFound) {
				throw new RuntimeException("Configuration resource not found: " + resourcePath);
			}
		}
	}

	public void combineConfigurationFromStream(InputStream stream)
			throws IncorrectConfigurationException, IOException {
		configuration.combine(stream);
	}

	/**
	 * Reads configuration from given file.
	 * @param file file with configuration
	 * @throws IncorrectConfigurationException when configuration is incorrect
	 * @throws IOException when cannot read file
	 */
	public void combineConfigurationFromFile(File file, boolean throwIfNotFound)
			throws IncorrectConfigurationException, IOException {
		if (!file.exists()) {
			if (throwIfNotFound) {
				throw new RuntimeException("Configuration file not found: " + file.getAbsolutePath());
			} else {
				return;
			}
		}
		try(InputStream stream = new FileInputStream(file)) {
			configuration.combine(stream);
		}
	}

	public String printCombinedXml() {
		return XmlUtils.prettyPrint(configuration.getRootElement());
	}

	/**
	 * Adds external module to the Services injector.
	 *
	 * Must be executed before {@link #start()}. One example of the usage is to register
	 * a module which will mock some objects during the testing.
	 *
	 * @param module module to be added
	 */
	public void addModule(Module module) {
		extraModules.add(module);
	}

	public void addCustomPropertyResolver(PropertyResolver resolver) {
		customPropertyResolvers.addPropertyResolver(resolver);
	}

	public void generateTemplateConfigurationFile() throws FileNotFoundException, IOException {
		configuration.generateSchema(new File(getConfigHome(), SCHEMA_FILE_NAME));
		File configurationFile = new File(getConfigHome(), CONFIG_FILE_NAME);
		if (configurationFile.exists()) {
			return;
		}
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(configurationFile), Charsets.UTF_8)) {
			writer.append("<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
					+ " xsi:noNamespaceSchemaLocation=\"" + SCHEMA_FILE_NAME
					+ "\">\n</config>\n");
		}
	}

	/**
	 * Enables printing of all registered Guice bindings during startup.
	 */
	public void enableGuiceBindingPrinting() {
		printGuiceBindings = true;
	}

	public void setup(ServicesCommandLineParameters params)
			throws IncorrectConfigurationException, IOException {
		if (params.getHomeDirectory() != null) {
			setHomeDirectory(new File(params.getHomeDirectory()));
		}
		if (params.getDataHome() != null) {
			setDataHome(new File(params.getDataHome()));
		}
		if (params.getConfigHome() != null) {
			setConfigHome(new File(params.getConfigHome()));
		}
		if (params.getCacheHome() != null) {
			setCacheHome(new File(params.getCacheHome()));
		}
		if (params.getDataDir() != null) {
			setDataDir(new File(params.getDataDir()));
		}
		List<File> dirs = new ArrayList<>();
		for (String dir : params.getConfigDirs()) {
			dirs.add(new File(dir));
		}
		setConfigDirs(dirs);
		if (params.getRuntimeDirectory() != null) {
			setRuntimeDirectory(new File(params.getRuntimeDirectory()));
		}
		if (!params.isNoDefaults()) {
			combineDefaultConfiguration();
		}
		if (params.getConfigurationFiles().isEmpty()) {
			combineConfigDirConfiguration();
		} else {
			for (String file : params.getConfigurationFiles()) {
				combineConfigurationFromFile(new File(file), true);
			}
		}

		if (params.isPrintConfig()) {
			System.out.println(printCombinedXml());
			System.exit(0);
		}

		if (params.isPrintFilteredConfig()) {
			filterConfiguration();
			System.out.println(printCombinedXml());
			System.exit(0);
		}

		if (params.isPrintGuiceBindings()) {
			enableGuiceBindingPrinting();
		}
	}

	private void filterConfiguration() throws IncorrectConfigurationException {
		Properties properties = new Properties();
		properties.setProperty("configHome", getConfigHome().getAbsolutePath());
		properties.setProperty("dataHome", getDataHome().getAbsolutePath());
		properties.setProperty("cacheHome", getCacheHome().getAbsolutePath());
		properties.setProperty("dataDir", getDataDir().getAbsolutePath());
		properties.setProperty("runtimeDirectory", getRuntimeDirectory().getAbsolutePath());

		Element propertiesElement = null;
		if (configuration.getRootElement() != null) {
			NodeList nodesList = configuration.getRootElement().getElementsByTagName("properties");
			if (nodesList.getLength() == 1) {
				propertiesElement = (Element) nodesList.item(0);
			}
		}

		propertyResolver = new CompoundPropertyResolver(
				new PropertiesPropertyResolver(properties),
				new SystemPropertyResolver(),
				new EnvironmentPropertyResolver(),
				new XmlPropertyResolver(propertiesElement, false),
				customPropertyResolvers,
				new XmlPropertyResolver(configuration.getRootElement(), true));

		configuration.filter(propertyResolver);
	}

	public static boolean isSingleton(Class<?> klass) {
		return klass.isAnnotationPresent(Singleton.class)
				|| klass.isAnnotationPresent(com.google.inject.Singleton.class);
	}

	private void verifySingletonServicesAreUnique(List<Service> services) {
		Set<Class<?>> set = new HashSet<>();
		for (Service service : services) {
			Class<?> klass = service.getClass();
			if (isSingleton(klass)) {
				if (set.contains(klass)) {
					throw new RuntimeException("Service '" + klass.getCanonicalName() + "' is marked"
							+ " as singleton, but is declared more than once in configuration file");
				}
				set.add(klass);

				if (!Strings.isNullOrEmpty(service.getId())) {
					throw new RuntimeException("Service '" + klass.getCanonicalName() + "' is marked"
							+ " as singleton, but has an id specified");
				}
			}
		}
	}

	private Injector buildInjector() {
		Map<Service, List<com.google.inject.spi.Element>> serviceElements = new LinkedHashMap<>();
		List<Module> modules = new ArrayList<>();
		DuplicateDetectionWrapper duplicateDetection = new DuplicateDetectionWrapper();

		// Use ServletModule specifically so @RequestScoped annotation will be always bound
		Module servletsModule = duplicateDetection.wrap(new ServletModule() {
			@Override
			public void configureServlets() {
				bind(Key.get(PropertyResolver.class, ApplicationProperties.class))
						.toInstance(propertyResolver);
				bind(Key.get(Boolean.class, ExternalContainer.class))
						.toInstance(externalContainer);
				bind(Services.class).toInstance(Services.this);
				binder().requireExplicitBindings();
			}
		});

		// important magic below:
		// Every ServletModule instance tries to install InternalServletModule. The trick is used, because Guice
		// installs modules only the first time and ignores any subsequent execution of install method
		// with the same module (by comparing them using equals() method).
		// We need to make sure InternalServletModule is installed in the top level module,
		// because when it is installed from some private module it doesn't have an access to all
		// registered servlets and filters.
		//
		// The line below makes sure first instance of ServletModule is rewritten in global scope
		modules.add(Elements.getModule(Elements.getElements(servletsModule)));

		for (Module module : extraModules) {
			modules.add(duplicateDetection.wrap(module));
		}

		for (Service service : config.getServices()) {
			logger.info("Configuring {}...", service.getClass().getSimpleName());
			Module module = service.configure();
			if (module != null) {
				serviceElements.put(service, Elements.getElements(duplicateDetection.wrap(module)));
			} else {
				serviceElements.put(service, Collections.<com.google.inject.spi.Element>emptyList());
			}
		}

		for (Map.Entry<Service, List<com.google.inject.spi.Element>> entry : serviceElements.entrySet()) {
			Service service = entry.getKey();
			List<com.google.inject.spi.Element> elements = entry.getValue();

			serviceElements.put(service, ServiceModuleRewriter.annotateExposedWithId(elements, service));
		}

		for (Map.Entry<Service, List<com.google.inject.spi.Element>> entry : serviceElements.entrySet()) {
			Service service = entry.getKey();
			List<com.google.inject.spi.Element> elements = entry.getValue();

			serviceElements.put(service, ServiceModuleRewriter.importBindings(elements, service, serviceElements));
		}

		for (Map.Entry<Service, List<com.google.inject.spi.Element>> entry : serviceElements.entrySet()) {
			List<com.google.inject.spi.Element> elements = entry.getValue();
			modules.add(Elements.getModule(elements));
		}

		modules.add(new InjectMembersModule());

		if (printGuiceBindings) {
			System.out.println("#############################");
			System.out.println("# Registered Guice bindings #");
			System.out.println("#############################");
			GuiceBindingsHelper.printServiceElements(serviceElements);
		}
		return Guice.createInjector(modules);
	}

	/**
	 * Reads configuration file and starts all services.
	 */
	public void start() {
		try {
			createDirectory(getHomeDirectory());
			createDirectory(getConfigHome());
			createDirectory(getDataHome());
			createDirectory(getCacheHome());

			filterConfiguration();

			config = configuration.read(Config.class);

			if (config == null) {
				config = new Config();
			}

			verifySingletonServicesAreUnique(config.getServices());
			injector = buildInjector();

			for (Service service : config.getServices()) {
				if (logger.isInfoEnabled()) {
					if (isStartMethodOverriden(service.getClass())) {
						logger.info("Starting {}...", service.getClass().getSimpleName());
					}
				}
				startedServices.add(service);
				service.start();
			}
			logger.info("Done");
		} catch (RuntimeException | IncorrectConfigurationException e) {
			try {
				stop();
			} catch (Exception f) {
				logger.error("Cannot properly stop services after previous fatal error", f);
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stops all services.
	 */
	public void stop() {
		if (config == null) {
			return;
		}

		for (Service service : startedServices) {
			logger.info("Stopping {}...", service.getClass().getSimpleName());
			service.stop();
		}
		for (Service service : config.getServices()) {
			service.deconfigure();
		}
		if (logger != null) {
			logger.info("All stopped");
		}
		injector = null;
	}

	/**
	 * Returns internal Services injector.
	 * @return injector
	 */
	public Injector injector() {
		return injector;
	}

	@Override
	protected Injector getInjector() {
		externalContainer = true;
		configHome = new File(".config");
		start();
		return injector;
	}

	private static boolean isStartMethodOverriden(Class<?> klass) {
		while (klass != Service.class) {
			try {
				klass.getDeclaredMethod("start");
			} catch (NoSuchMethodException e) {
				klass = klass.getSuperclass();
				continue;
			}
			return true;
		}
		return false;
	}
}

