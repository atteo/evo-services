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
package org.atteo.evo.jmx;

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

import org.atteo.evo.services.ImportBindings;
import org.atteo.evo.services.TopLevelService;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * Enables remote JMX monitoring.
 *
 * @see <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html">Monitoring and Management Using JMX Technology</a>
 */
@XmlRootElement(name = "remoteJmx")
public class RemoteJmx extends TopLevelService {
	/**
	 * ID of selected {@link RmiRegistry RMI registry}.
	 */
	@XmlElement
	@XmlIDREF
	@ImportBindings
	private RmiRegistry rmiRegistry;

	@Inject
	private RmiRegistryPort portProvider;

	@Override
	public void start() {
		JMXServiceURL url;
		try {
			url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + portProvider.getPort() + "/jmxrmi");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		HashMap<String,Object> environment = new HashMap<>();
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		JMXConnectorServer connectorServer;
		try {
			connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, mbeanServer);
			connectorServer.start();
		} catch (IOException e) {
			throw new RuntimeException("Cannot start JMX connector server", e);
		}
	}
}
