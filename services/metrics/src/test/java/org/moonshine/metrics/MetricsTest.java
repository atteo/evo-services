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
package org.moonshine.metrics;

import java.io.IOException;

import javax.inject.Inject;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.jmx.JmxUtils;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Test;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;

@MoonshineConfiguration(autoConfiguration = true)
public class MetricsTest extends MoonshineTest {
	@Inject
	private MBeanServer mbeanServer;

	@Inject
	private MetricRegistry metrics;

	@Test
	public void shouldMeterManuallyRegistered() throws IOException, InterruptedException, JMException {
		// given
		final String metricName = MetricRegistry.name(MetricsTest.class, "constant");
		metrics.register(metricName, (Gauge<Long>) () -> 3l);

		// when
		ObjectName objectName = ObjectName.getInstance("metrics:name=" + MetricsTest.class.getName() + ".constant");
		Long value = (Long) mbeanServer.getAttribute(objectName, "Value");

		// then
		assertThat(value).isEqualTo(3l);

		metrics.remove(metricName);
	}

	@Metered
	protected void meteredMethod() {
	}

	@Test
	public void shouldMeterAnnotated() throws IOException, JMException {
		// given
		meteredMethod();
		meteredMethod();

		// when
		JMXConnector connector = JmxUtils.connectToItself();
		MBeanServerConnection server = connector.getMBeanServerConnection();
		ObjectName objectName = ObjectName.getInstance("metrics:name=" + MetricsTest.class.getName()
				+ ".meteredMethod");
		Long value = (Long) server.getAttribute(objectName, "Count");

		// then
		assertThat(value).isEqualTo(2);
	}
}
