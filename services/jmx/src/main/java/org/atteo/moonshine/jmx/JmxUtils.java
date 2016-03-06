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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class JmxUtils {

	private JmxUtils() {
	}

	public static long getVirtualMachinePid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		return Long.parseLong(name.substring(0, name.indexOf('@')));
	}

	public static JMXServiceURL attachToJMX(Long pid) {
		String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";
		VirtualMachine vm;
		try {
			vm = VirtualMachine.attach(pid.toString());
		} catch (AttachNotSupportedException | IOException e) {
			throw new RuntimeException(e);
		}
		try {
			String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
			if (connectorAddress == null) {
				String agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
				vm.loadAgent(agent);
				connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
			}
			return new JMXServiceURL(connectorAddress);
		} catch (IOException | AgentLoadException | AgentInitializationException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				vm.detach();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static JMXConnector connectToItself() throws IOException {
		return JMXConnectorFactory.connect(JmxUtils.attachToJMX(JmxUtils.getVirtualMachinePid()));
	}

}
