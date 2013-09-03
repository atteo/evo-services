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
package org.atteo.moonshine.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.services.TopLevelService;

/**
 * Publishes JMX MBeanServer through RMI.
 *
 * @see <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html">Monitoring and Management Using JMX Technology</a>
 */
@XmlRootElement(name = "publishJmx")
public class PublishJmx extends TopLevelService {
	/**
	 * JMX MBeanServer to publish.
	 */
	@XmlElement
	@XmlIDREF
	@ImportService
	private JMX jmx;

	/**
	 * {@link RmiRegistry RMI registry} on publish MBeanServer onto.
	 */
	@XmlElement
	@XmlIDREF
	@ImportService
	private RmiRegistry rmiRegistry;

	@Inject
	private RmiRegistryPort portProvider;

	@Inject
	private MBeanServer mbeanServer;

	@Override
	public void start() {
		JMXServiceURL url;
		try {
			url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + portProvider.getPort() + "/jmxrmi");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		HashMap<String,Object> environment = new HashMap<>();
		JMXConnectorServer connectorServer;
		try {
			connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, mbeanServer);
			connectorServer.start();
		} catch (IOException e) {
			throw new RuntimeException("Cannot start JMX connector server", e);
		}
	}
}
