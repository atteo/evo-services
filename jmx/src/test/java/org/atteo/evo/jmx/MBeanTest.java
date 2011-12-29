/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.evo.jmx;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.atteo.evo.tests.ServicesRule;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;

public class MBeanTest {
	@Rule
	public static ServicesRule services = new ServicesRule();

	@Test
	public void getData() throws IOException, MalformedObjectNameException {
		JMXConnector connector = JMXConnectorFactory.connect(getServiceUrl(getVirtualMachinePid()));
		MBeanServerConnection server = connector.getMBeanServerConnection();

		ObjectName name = ObjectName.getInstance(Car.class.getPackage().getName()
				+ ":type=" + Car.class.getSimpleName());
		CarMBean proxy = javax.management.JMX.newMBeanProxy(server, name, CarMBean.class);

		assertEquals("blue", proxy.getColor());
	}

	private JMXServiceURL getServiceUrl(Long pid) {
		String CONNECTOR_ADDRESS =
				"com.sun.management.jmxremote.localConnectorAddress";

		// attach to the target application
		com.sun.tools.attach.VirtualMachine vm;
		try {
			vm = com.sun.tools.attach.VirtualMachine.attach(pid.toString());
		} catch (AttachNotSupportedException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			// get the connector address
			String connectorAddress =
					vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

			// no connector address, so we start the JMX agent
			if (connectorAddress == null) {
				String agent = vm.getSystemProperties().getProperty("java.home") +
						File.separator + "lib" + File.separator +
						"management-agent.jar";
				vm.loadAgent(agent);

				// agent is started, get the connector address
				connectorAddress =
						vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
			}

			// establish connection to connector server
			return new JMXServiceURL(connectorAddress);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (AgentLoadException e) {
			throw new RuntimeException(e);
		} catch (AgentInitializationException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				vm.detach();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private long getVirtualMachinePid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		return Long.parseLong(name.substring(0, name.indexOf('@')));
	}
}
