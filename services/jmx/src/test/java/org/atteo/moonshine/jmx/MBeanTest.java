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
package org.atteo.moonshine.jmx;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

@MoonshineConfiguration(autoConfiguration = true)
public class MBeanTest extends MoonshineTest {
	@Test
	public void getData() throws IOException, MalformedObjectNameException {
		JMXConnector connector = JmxUtils.connectToItself();
		MBeanServerConnection server = connector.getMBeanServerConnection();

		ObjectName name = ObjectName.getInstance(Car.class.getPackage().getName()
				+ ":type=" + Car.class.getSimpleName());
		CarMBean proxy = javax.management.JMX.newMBeanProxy(server, name, CarMBean.class);

		assertEquals("blue", proxy.getColor());
	}
}
