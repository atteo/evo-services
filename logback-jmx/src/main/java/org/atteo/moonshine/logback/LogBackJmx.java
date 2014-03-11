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
package org.atteo.moonshine.logback;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.jmx.Jmx;
import org.atteo.moonshine.services.ImportService;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jmx.JMXConfigurator;

/**
 * LogBackJmx JMX support.
 *
 * <p>
 LogBackJmx MBeans allow you to change logging levels in running application.
 </p>
 */
@XmlRootElement(name = "logback-jmx")
public class LogBackJmx extends TopLevelService {
	@ImportService
	private Jmx jmx;

	@Inject
	private MBeanServer mbeanServer;

	@Override
	public void start() {
		configureJMX();
	}

	@Override
	public void close() {
		deconfigureJMX();
	}

	private JMXConfigurator jmxConfigurator;

	private void configureJMX() throws RuntimeException {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			ObjectName name = ObjectName.getInstance(JMXConfigurator.class.getPackage().getName()
					+ ":type=" + JMXConfigurator.class.getSimpleName());
			jmxConfigurator = new JMXConfigurator(context, mbeanServer, name);
			if (!mbeanServer.isRegistered(name)) {
				mbeanServer.registerMBean(jmxConfigurator, name);
			}
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
				| NotCompliantMBeanException e) {
			throw new RuntimeException(e);
		}
	}

	private void deconfigureJMX() {
		// force JMXConfigurator to deregister itself from MBean server
		jmxConfigurator.onStop(null);
	}
}
