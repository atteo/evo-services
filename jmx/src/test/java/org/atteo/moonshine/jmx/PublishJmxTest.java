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
import java.net.MalformedURLException;

import javax.inject.Inject;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.atteo.moonshine.tests.ServicesConfiguration;
import org.atteo.moonshine.tests.ServicesTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;


@ServicesConfiguration("/test-publishjmx.xml")
public class PublishJmxTest extends ServicesTest {
	@Inject
	private RmiRegistryPort portProvider;

	@Test
	public void dummy() throws MalformedURLException, IOException, MalformedObjectNameException {
		JMXServiceURL u = new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://localhost:" + portProvider.getPort()
				+ "/jmxrmi");
		JMXConnector connector = JMXConnectorFactory.connect(u);

		MBeanServerConnection server = connector.getMBeanServerConnection();

		ObjectName name = ObjectName.getInstance(Car.class.getPackage().getName()
				+ ":type=" + Car.class.getSimpleName());
		CarMBean proxy = javax.management.JMX.newMBeanProxy(server, name, CarMBean.class);
		assertEquals("blue", proxy.getColor());
	}
}
