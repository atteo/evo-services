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
package org.atteo.moonshine.hornetq;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.atteo.moonshine.tests.MoonshineTest;
import static org.junit.Assert.*;
import org.junit.Test;

public class HornetQTest extends MoonshineTest {
	@Inject
	private ConnectionFactory connectionFactory;

	@Inject
	private UserTransaction transaction;

	@Test
	public void simple() throws JMSException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException, IllegalStateException, SystemException,
			NotSupportedException {
		transaction.begin();

		Connection connection = connectionFactory.createConnection();
		Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
		Queue queue = session.createQueue("testQueue");
		MessageProducer producer = session.createProducer(queue);
		connection.start();
		TextMessage message = session.createTextMessage("Hello World!!!");
		producer.send(message);
		producer.close();
		session.close();
		connection.close();

		transaction.commit();

		transaction.begin();

		connection = connectionFactory.createConnection();
		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		queue = session.createQueue("testQueue");
		MessageConsumer consumer = session.createConsumer(queue);
		message = (TextMessage) consumer.receiveNoWait();
		assertNotNull(message);
		assertEquals("Hello World!!!", message.getText());

		consumer.close();
		session.close();
		connection.close();

		transaction.commit();
	}

	@Test
	public void rollback() throws JMSException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException, IllegalStateException, SystemException,
			NotSupportedException {
		transaction.begin();

		Connection connection = connectionFactory.createConnection();
		Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
		Queue queue = session.createQueue("testQueue");
		MessageProducer producer = session.createProducer(queue);
		connection.start();
		TextMessage message = session.createTextMessage("Hello World!!!");
		producer.send(message);
		producer.close();
		session.close();
		connection.close();

		transaction.rollback();

		transaction.begin();

		connection = connectionFactory.createConnection();
		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		queue = session.createQueue("testQueue");
		MessageConsumer consumer = session.createConsumer(queue);
		message = (TextMessage) consumer.receiveNoWait();
		assertNull(message);

		consumer.close();
		session.close();
		connection.close();

		transaction.commit();
	}
}
