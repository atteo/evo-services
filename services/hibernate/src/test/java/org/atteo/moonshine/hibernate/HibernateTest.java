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
package org.atteo.moonshine.hibernate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.atteo.moonshine.jta.Transaction;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

@MoonshineConfiguration(autoConfiguration = true, fromString = ""
		+ "<config>"
		+ "    <hibernate>"
		+ "        <initSchema>create</initSchema>"
		+ "    </hibernate>"
		+ "</config>")
public class HibernateTest extends MoonshineTest {
	@Inject
	private UserTransaction transaction;

	@Inject
	private EntityManagerFactory factory;

	@Test
	public void shouldSaveUser() {
		final User u = new User();
		Transaction.require((Transaction.Runnable) () -> {
			EntityManager manager = factory.createEntityManager();
			u.setId(7);
			u.setName("frank");
			manager.persist(u);
			
			manager.flush();
			manager.close();
		});

		final int id = u.getId();

		Transaction.require((Transaction.Runnable) () -> {
			EntityManager manager = factory.createEntityManager();
			User loaded = manager.find(User.class, id);
			
			assertNotNull(loaded);
			assertEquals(u.getName(), loaded.getName());
			manager.close();
		});
	}

	// Based on http://en.wikibooks.org/wiki/Java_Persistence/Transactions
	@Test
	public void shouldJoinTransaction() throws NotSupportedException, SystemException, RollbackException,
			HeuristicMixedException, HeuristicRollbackException {
		EntityManager manager = factory.createEntityManager();
		User u = new User();
		u.setId(8);
		u.setName("frank");
		manager.persist(u);
		int id = u.getId();

		transaction.begin();
		manager.joinTransaction();
		manager.close();
		transaction.commit();

		transaction.begin();
		manager = factory.createEntityManager();
		User loaded = manager.find(User.class, id);

		assertNotNull(loaded);
		assertEquals(u.getName(), loaded.getName());

		manager.close();
		transaction.rollback();
	}
}

