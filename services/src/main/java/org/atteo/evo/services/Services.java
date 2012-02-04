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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.config.Configuration;
import org.atteo.evo.filtering.CompoundPropertyResolver;
import org.atteo.evo.filtering.EnvironmentPropertyResolver;
import org.atteo.evo.filtering.PropertiesPropertyResolver;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.evo.filtering.RecursivePropertyResolver;
import org.atteo.evo.filtering.SystemPropertyResolver;
import org.atteo.evo.filtering.XmlPropertyResolver;
import org.atteo.evo.injection.InjectMembersModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

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
 * Read the description of the {@link Configuration} engine to learn more about merging, filgering
 * and validating configuration file.
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
	private File applicationHome;
	private File configurationFile;
	private InputStream configurationStream;
	private InputStream parentConfigurationStream;
	private File warDirectory;
	private Injector injector;
	private boolean externalContainer = false;
	private List<Module> modules = new ArrayList<Module>();
	private Logger logger = LoggerFactory.getLogger("Evo Services");
	private Config config;
	private List<Service> startedServices = new ArrayList<Service>();
	private CompoundPropertyResolver customPropertyResolvers = new CompoundPropertyResolver();

	public Services() {
	}

	public Services(File applicationHome, File warDirectory) {
		this.applicationHome = applicationHome;
		this.warDirectory = warDirectory;
		configurationFile = new File(applicationHome, "config.xml");
		parentConfigurationStream =
				getClass().getResourceAsStream("/default-config.xml");
	}

	public Services(File applicationHome, File warDirectory, InputStream configurationStream) {
		this(applicationHome, warDirectory);

		configurationFile = null;
		this.configurationStream = configurationStream;
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
		modules.add(module);
	}

	public void addCustomPropertyResolver(PropertyResolver resolver) {
		customPropertyResolvers.addPropertyResolver(resolver);
	}

	/**
	 * Reads configuration file and starts all services.
	 */
	public void start() {
		applicationHome = applicationHome.getAbsoluteFile(); // exists() fails for "" (current dir)
		if (!applicationHome.exists() && !applicationHome.mkdirs()) {
			throw new RuntimeException("Cannot create application directory: "
					+ applicationHome.getAbsolutePath());
		}
		// first log message, triggers reading logback.xml if LogBack is used
		logger.info("Booting...");

		// Redirect messages from JUL through SLF4J
		SLF4JBridgeHandler.install();

		try {
			Configuration configuration = new Configuration();

			if (configurationFile != null) {
				final String schemaFileName = "schema.xsd";
				configuration.generateSchema(new File(applicationHome, schemaFileName));

				if (!configurationFile.exists()) {
					Writer writer = new FileWriter(configurationFile);
					writer.append("<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
							+ " xsi:noNamespaceSchemaLocation=\"" + schemaFileName
							+ "\">\n</config>\n");
					writer.close();
				}

				configurationStream = new FileInputStream(configurationFile);
			}

			if (parentConfigurationStream != null) {
				configuration.combine(parentConfigurationStream);
			}

			if (configurationStream != null) {
				configuration.combine(configurationStream);
			}

			Properties properties = new Properties();
			properties.setProperty("applicationHome", applicationHome.getAbsolutePath());
			Element propertiesElement = null;
			NodeList nodesList = configuration.getRootElement().getElementsByTagName("properties");
			if (nodesList.getLength() == 1) {
				propertiesElement = (Element) nodesList.item(0);
			}

			for (Class<?> klass : ClassIndex.getAnnotated(ApplicationProperties.class)) {
				PropertyResolver s = (PropertyResolver) klass.newInstance();
				customPropertyResolvers.addPropertyResolver(s);
			}

			final PropertyResolver propertyResolver = new RecursivePropertyResolver(
					new CompoundPropertyResolver(
							new PropertiesPropertyResolver(properties),
							new SystemPropertyResolver(),
							new EnvironmentPropertyResolver(),
							new XmlPropertyResolver(propertiesElement, false),
							customPropertyResolvers,
							new XmlPropertyResolver(configuration.getRootElement(), true)
					));

			configuration.setPropertyResolver(propertyResolver);

			config = configuration.read(Config.class);

			if (config == null) {
				config = new Config();
			}

			for (Service service : config.getServices()) {
				Module module = service.configure();
				if (module != null) {
					modules.add(module);
				}
			}

			// Use ServletModule specifically so @RequestScoped annotation will be bound
			modules.add(new ServletModule() {
				@Override
				public void configureServlets() {
					bind(Key.get(PropertyResolver.class, ApplicationProperties.class))
							.toInstance(propertyResolver);
					if (warDirectory != null) {
						bind(Key.get(File.class, ContentDirectory.class))
							.toInstance(warDirectory);
					}
					bind(Key.get(Boolean.class, ExternalContainer.class))
							.toInstance(externalContainer);
					bind(Services.class).toInstance(Services.this);
				}
			});
			modules.add(new InjectMembersModule());

			injector = Guice.createInjector(modules);

			injector.injectMembers(config);

			for (Service service : config.getServices()) {
				logger.info("Starting {}...", service.getClass().getSimpleName());
				service.start();
				startedServices.add(service);
			}
			logger.info("Done");
		} catch (Exception e) {
			logger.error("Stopping due to the fatal error", e);

			stop();

			throw new RuntimeException("Fatal error", e);
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
		String root = ".config";
		applicationHome = new File(root);
		configurationFile = new File(applicationHome, "config.xml");
		parentConfigurationStream =
				getClass().getResourceAsStream("/default-config.xml");
		start();
		return injector;
	}
}

