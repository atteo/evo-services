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
package org.atteo.moonshine.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.config.Configuration;
import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.config.XmlUtils;
import org.atteo.evo.filtering.CompoundPropertyResolver;
import org.atteo.evo.filtering.EnvironmentPropertyResolver;
import org.atteo.evo.filtering.OneOfPropertyResolver;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.evo.filtering.SystemPropertyResolver;
import org.atteo.evo.filtering.XmlPropertyResolver;
import org.atteo.evo.urlhandlers.UrlHandlers;
import org.atteo.moonshine.directories.FileAccessor;
import org.atteo.moonshine.injection.InjectMembersModule;
import org.atteo.moonshine.services.internal.DuplicateDetectionWrapper;
import org.atteo.moonshine.services.internal.GuiceBindingsHelper;
import org.atteo.moonshine.services.internal.ServiceModuleRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.Elements;

/**
 * Moonshine is a runtime service engine based on Google Guice
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
 * The use of SLF4J is mandatory. By default Moonshine has dependency on LogBack in POM file
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
public class Services {
	public final static String SCHEMA_FILE_NAME = "schema.xsd";
	public final static String CONFIG_FILE_NAME = "config.xml";
	public final static String DEFAULT_CONFIG_RESOURCE_NAME = "/default-config.xml";
	private final Logger logger = LoggerFactory.getLogger("Moonshine");
	private List<Module> extraModules = new ArrayList<>();
	private CompoundPropertyResolver customPropertyResolvers = new CompoundPropertyResolver();
	private FileAccessor fileAccessor;

	private Configuration configuration;
	private Injector injector;
	private boolean printGuiceBindings = false;
	private Config config;
	private PropertyResolver propertyResolver;
	private List<Service> startedServices = new ArrayList<>();
	private Map<Service, String> serviceNameMap = new IdentityHashMap<>();


	public Services(String applicationName, FileAccessor fileAccessor) {
		this.fileAccessor = fileAccessor;

		configuration = new Configuration();

		UrlHandlers.registerAnnotatedHandlers();
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
		for (Path path : fileAccessor.getConfigFiles(CONFIG_FILE_NAME)) {
			try (InputStream stream = Files.newInputStream(path, StandardOpenOption.READ)) {
				combineConfigurationFromStream(stream);
			}
		}
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

	/**
	 * Reads configuration from given string.
	 * @param string string with configuration
	 * @throws IncorrectConfigurationException when configuration is incorrect
	 */
	public void combineConfigurationFromString(String string) throws IncorrectConfigurationException {
		try (InputStream stream = new ByteArrayInputStream(string.getBytes(Charsets.UTF_8))) {
			configuration.combine(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
		Path schemaPath = fileAccessor.getWritebleConfigFile(SCHEMA_FILE_NAME);
		configuration.generateSchema(schemaPath.toFile());

		Path configPath = fileAccessor.getWritebleConfigFile(CONFIG_FILE_NAME);
		if (Files.exists(configPath)) {
			return;
		}
		try (Writer writer = Files.newBufferedWriter(configPath, Charsets.UTF_8)) {
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
		Element propertiesElement = null;
		if (configuration.getRootElement() != null) {
			NodeList nodesList = configuration.getRootElement().getElementsByTagName("properties");
			if (nodesList.getLength() == 1) {
				propertiesElement = (Element) nodesList.item(0);
			}
		}

		propertyResolver = new CompoundPropertyResolver(
				new OneOfPropertyResolver(),
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

	private void verifySingletonServicesAreUnique(List<Service> services) throws IncorrectConfigurationException {
		Set<Class<?>> set = new HashSet<>();
		for (Service service : services) {
			Class<?> klass = service.getClass();
			if (isSingleton(klass)) {
				if (set.contains(klass)) {
					throw new IncorrectConfigurationException("Service '" + klass.getCanonicalName() + "' is marked"
							+ " as singleton, but is declared more than once in configuration file");
				}
				set.add(klass);

				if (!Strings.isNullOrEmpty(service.getId())) {
					throw new IncorrectConfigurationException("Service '" + klass.getCanonicalName() + "' is marked"
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
				bind(Key.get(PropertyResolver.class, ApplicationProperties.class)).toInstance(propertyResolver);
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

		for (Service service : config.getSubServices()) {
			logger.info("Configuring: {}", serviceNameMap.get(service));
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

		List<String> hints = new ArrayList<>();

		for (Map.Entry<Service, List<com.google.inject.spi.Element>> entry : serviceElements.entrySet()) {
			Service service = entry.getKey();
			List<com.google.inject.spi.Element> elements = entry.getValue();

			serviceElements.put(service, ServiceModuleRewriter.importBindings(elements, service, serviceElements,
					hints));
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
		logger.info("Building Guice injector");
		try {
			return Guice.createInjector(modules);
		} catch (CreationException e) {
			if (!hints.isEmpty()) {
				logger.warn("Problem detected while creating Guice injector, possible causes:");
				for (String hint : hints) {
					logger.warn(" -> " + hint);
				}
			}
			throw e;
		}
	}

	/**
	 * Reads configuration file and starts all services.
	 */
	public void start() throws IncorrectConfigurationException {
		logger.info("Initializing services");
		filterConfiguration();

		config = configuration.read(Config.class);

		if (config == null) {
			config = new Config();
		}

		buildServiceNameMap(config.getSubServices());
		verifySingletonServicesAreUnique(config.getSubServices());
		injector = buildInjector();

		for (Service service : config.getSubServices()) {
			if (logger.isInfoEnabled()) {
				if (isMethodOverriden(service.getClass(), "start")) {
					logger.info("Starting: {}", serviceNameMap.get(service));
				}
			}
			startedServices.add(service);
			service.start();
		}
		logger.info("All services started");
	}

	/**
	 * Stops all services.
	 */
	public void stop() {
		if (config == null) {
			return;
		}

		for (Service service : startedServices) {
			String name = serviceNameMap.get(service);
			if (name == null) {
				name = service.getClass().getSimpleName();
			}
			logger.info("Stopping: {}", name);
			service.stop();
		}
		for (Service service : config.getSubServices()) {
			service.deconfigure();
		}
		if (logger != null) {
			logger.info("All services stopped");
		}
		injector = null;
	}

	/**
	 * Returns the global injector.
	 */
	public Injector getGlobalInjector() {
		return injector;
	}

	private static boolean isMethodOverriden(Class<?> klass, String methodName) {
		while (klass != Service.class) {
			try {
				klass.getDeclaredMethod(methodName);
			} catch (NoSuchMethodException e) {
				klass = klass.getSuperclass();
				continue;
			}
			return true;
		}
		return false;
	}

	private void buildServiceNameMap(List<Service> services) {
		for (Service service : services) {
			StringBuilder builder = new StringBuilder();

			if (service.getId() != null) {
				builder.append("\"");
				builder.append(service.getId());
				builder.append("\" ");
			}

			String summary = ClassIndex.getClassSummary(service.getClass());
			builder.append(service.getClass().getSimpleName());
			if (summary != null) {
				builder.append(" (");
				builder.append(summary);
				builder.append(")");
			}
			serviceNameMap.put(service, builder.toString());
		}
	}
}

