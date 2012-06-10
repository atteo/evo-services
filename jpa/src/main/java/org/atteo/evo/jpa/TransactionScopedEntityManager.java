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
package org.atteo.evo.jpa;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

public class TransactionScopedEntityManager extends DelegatingEntityManager {
	@Inject
	private EntityManagerFactory factory;

	@Inject
	private TransactionManager transactionManager;
	
	private EntityManager entityManager = null;

	@Override
	protected EntityManager getEntityManager() {
		if (entityManager == null) {
			entityManager = factory.createEntityManager();

			try {
				Transaction transaction = transactionManager.getTransaction();
				if (transaction == null) {
					throw new RuntimeException("Not in transaction. Initiate transaction in JTA.");
				}
				transaction.registerSynchronization(new Synchronization() {
					@Override
					public void beforeCompletion() {
					}
					
					@Override
					public void afterCompletion(int status) {
						if (entityManager != null) {
							entityManager.close();
						}
					}
				});
			} catch (SystemException e) {
				throw new RuntimeException(e);
			} catch (RollbackException e) {
				throw new RuntimeException(e);
			}
		}

		return entityManager;
	}
}
