/*
 * Copyright 2012 Atteo.
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
package org.atteo.moonshine.hibernate;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.atteo.moonshine.jta.Transaction;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <atomikos/>"
		+ "    <transactional/>"
		+ "    <h2/>"
		+ "    <hibernate>"
		+ "        <initSchema>create</initSchema>"
		+ "    </hibernate>"
		+ "</config>")
public class EntityManagerTest extends MoonshineTest {
	@Inject
	private EntityManager entityManager;

	@Test
	public void shouldInjectEntityManager() {
		Transaction.require(new Transaction.Runnable() {
			@Override
			public void run() {
				assertTrue(entityManager.isOpen());
				User entity = new User();
				entity.setId(0);
				entityManager.persist(entity);
			}
		});

		assertFalse(entityManager.isOpen());
	}
}
