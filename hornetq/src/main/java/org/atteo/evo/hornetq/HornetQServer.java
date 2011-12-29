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
package org.atteo.evo.hornetq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.evo.services.TopLevelService;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.server.JournalType;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Starts HornetQ JMS server.
 * 
 * <p>
 * Currently the transport supports only clients in the same JVM.
 * </p>
 */
@XmlRootElement(name = "hornetq-server")
public class HornetQServer extends TopLevelService {
	/**
	 * List of JMS queues to create.
	 */
	@XmlElementWrapper(name = "queues")
	@XmlElement(name = "queue")
	List<String> queues;

	@XmlElement
	@XmlDefaultValue("${applicationHome}/jms/journal")
	private String journalDirectory;

	@XmlElement
	@XmlDefaultValue("${applicationHome}/jms/bindings")
	private String bindingsDirectory;

	@XmlElement
	@XmlDefaultValue("${applicationHome}/jms/largeMessages")
	private String largeMessagesDirectory;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
			}
		};
	}

	@Inject
	private Injector injector;

	private EmbeddedJMS jms;

	@Override
	public void start() {
		jms = new EmbeddedJMS();

		List<ConnectionFactoryConfiguration> connections = new ArrayList<ConnectionFactoryConfiguration>();
		List<JMSQueueConfiguration> queueConfigs =  new ArrayList<JMSQueueConfiguration>();
		for (String queue : queues) {
			queueConfigs.add(new JMSQueueConfigurationImpl(queue, "", true, ""));
		}
		List<TopicConfiguration> topics = new ArrayList<TopicConfiguration>();
		JMSConfiguration jmsConfig = new JMSConfigurationImpl(connections, queueConfigs, topics, null);

		jms.setJmsConfiguration(jmsConfig);

		Configuration config = new ConfigurationImpl();
		config.setSecurityEnabled(false);
		config.setJournalType(JournalType.NIO);
		config.setJMXManagementEnabled(true);
		config.setPersistenceEnabled(true);
		config.setJournalDirectory(journalDirectory);
		config.setBindingsDirectory(bindingsDirectory);
		config.setLargeMessagesDirectory(largeMessagesDirectory);

		Set<TransportConfiguration> transports = new HashSet<TransportConfiguration>();
		transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
		config.setAcceptorConfigurations(transports);

		jms.setConfiguration(config);
		try {
			jms.start();
		} catch (Exception e) {
			throw new RuntimeException("Cannot initialize JMS server", e);
		}
	}

	@Override
	public void stop() {
		try {
			jms.stop();
		} catch (Exception e) {
			throw new RuntimeException("Cannot stop JMS server", e);
		}
	}
}
