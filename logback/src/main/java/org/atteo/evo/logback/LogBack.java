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
package org.atteo.evo.logback;

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.inject.Inject;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.evo.services.TopLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * LogBack configuration.
 *
 * <p>
 * During the early startup LogBack searches for the logback.xml configuration file
 * in classpath. Services framework comes with the default file which prints
 * all INFO and higher messages to the standard output. If you want to change that behavior
 * we encourage you to provide your own logback.xml file in the classpath.
 * Although {@link #configFile} attribute allows you to reconfigure LogBack with the specified
 * configuration file, keep in mind that this happens late in the boot process.
 * </p>
 * <p>
 * {@link #jmx} attribute turns on JMX support. It allows you to control logging levels by connecting
 * to the running application with JConsole.
 * </p>
 * <p>
 * This service also registers LogBack's {@link LevelChangePropagator} which improves performance
 * for JUL logging, see <a href="http://logback.qos.ch/manual/configuration.html#LevelChangePropagator">LevelChangePropagator</a>.
 * </p>
 *
 */
@XmlRootElement(name = "logback")
public class LogBack extends TopLevelService {
	/**
	 * Reconfigure LogBack with provided configuration file.
	 */
	@XmlElement
	private String configFile = null;

	/**
	 * Register JMX MBean to control LogBack.
	 */
	@XmlElement(defaultValue = "false")
	private boolean jmx = false;

	@XmlDefaultValue("${applicationHome}")
	private File home;

	@Inject
	Logger logger;

	@Override
	public void start() {

		if (configFile != null) {
			File file = new File(configFile);
			if (!file.isAbsolute()) {
				file = new File(home, configFile);
			}
			if (!file.exists()) {
				throw new RuntimeException("Logback configuration file not found: " + configFile);
			}
			loadConfig(file);
		} else {
			File file = new File(home, "logback.xml");
			if (file.exists()) {
				loadConfig(file);
			}
		}

		// Propagate logging levels to JUL for performance reasons, for details see:
		// http://logback.qos.ch/manual/configuration.html#LevelChangePropagator
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.addListener(new LevelChangePropagator());

		if (jmx) {
			configureJMX();
		}
	}

	@Override
	public void stop() {
		if (jmx) {
			deconfigureJMX();
		}
	}

	private JMXConfigurator jmxConfigurator;

	private void loadConfig(File file) {
		logger.info("Reconfiguring logger with " + file.getPath());
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		// the context was probably already configured by default configuration
		// rules
		context.reset();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			configurator.doConfigure(file);
		} catch (JoranException e) {
			throw new RuntimeException(e);
		}
	}

	private void configureJMX() throws RuntimeException {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = ObjectName.getInstance(JMXConfigurator.class.getPackage().getName()
					+ ":type=" + JMXConfigurator.class.getSimpleName());
			jmxConfigurator = new JMXConfigurator(context, mbeanServer, name);
			if (!mbeanServer.isRegistered(name)) {
				mbeanServer.registerMBean(jmxConfigurator, name);
			}
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		} catch (InstanceAlreadyExistsException e) {
			throw new RuntimeException(e);
		} catch (MBeanRegistrationException e) {
			throw new RuntimeException(e);
		} catch (NotCompliantMBeanException e) {
			throw new RuntimeException(e);
		}
	}

	private void deconfigureJMX() {
		// force JMXConfigurator to deregister itself from MBean server
		jmxConfigurator.onStop(null);
	}
}
