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
package org.atteo.moonshine.jpa;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.atteo.moonshine.jta.Transaction;
import org.atteo.moonshine.tests.ServicesTest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class JpaTest extends ServicesTest {
	@Inject
	private EntityManager entityManager;

	@Test
	public void trivial() {
		Transaction.require(new Transaction.Runnable() {
			@Override
			public void run() {
				assertTrue(entityManager.isOpen());
				SampleEntity entity = new SampleEntity();
				entity.setId(0);
				entityManager.persist(entity);
			}
		});

		assertFalse(entityManager.isOpen());
	}
}
