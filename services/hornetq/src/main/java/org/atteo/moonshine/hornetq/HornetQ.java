/*
 * Copyright 2011 Atteo.
 *
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
package org.atteo.moonshine.hornetq;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.jta.JtaConnectionFactoryWrapper;
import org.atteo.moonshine.jta.PoolOptions;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.jms.client.HornetQXAConnectionFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;

/**
 * HornetQ JMS Client service.
 *
 * <p>
 * Currently is able to connect only to the {@link HornetQServer}
 * started in the same JVM.
 * </p>
 */
@XmlRootElement(name = "hornetq")
public class HornetQ extends TopLevelService {
	@XmlElement
	private PoolOptions pool;

	private ConnectionFactory connectionFactory;

	@Singleton
	private class ConnectionFactoryProvider implements Provider<ConnectionFactory> {
		@Inject
		private JtaConnectionFactoryWrapper wrapper;

		@Override
		public ConnectionFactory get() {
			TransportConfiguration transportConfiguration =
					new TransportConfiguration(InVMConnectorFactory.class.getName());
			XAConnectionFactory xaConnectionFactory = (HornetQXAConnectionFactory)
					HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.XA_CF,
					transportConfiguration);

			String name = "defaultConnectionFactory";
			if (getId() != null) {
				name = getId();
			}
			connectionFactory = wrapper.wrap(name, xaConnectionFactory, pool);
			return connectionFactory;
		}
	}

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(ConnectionFactory.class).toProvider(new ConnectionFactoryProvider()).in(Singleton.class);
			}
		};
	}

	@Inject
	private JtaConnectionFactoryWrapper wrapper;

	@Override
	public void close() {
		if (connectionFactory != null) {
			wrapper.close(connectionFactory);
			connectionFactory = null;
		}
	}
}
