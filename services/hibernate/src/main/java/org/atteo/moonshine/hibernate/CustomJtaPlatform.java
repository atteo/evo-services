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
import javax.inject.Provider;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.service.jta.platform.internal.JtaSynchronizationStrategy;
import org.hibernate.service.jta.platform.internal.TransactionManagerAccess;
import org.hibernate.service.jta.platform.internal.TransactionManagerBasedSynchronizationStrategy;
import org.hibernate.service.jta.platform.spi.JtaPlatform;

@SuppressWarnings("serial")
public class CustomJtaPlatform implements JtaPlatform {
	@Inject
	private Provider<TransactionManager> manager;

	@Inject
	private Provider<UserTransaction> userTransaction;

	private final JtaSynchronizationStrategy synchronizationStrategy =
			new TransactionManagerBasedSynchronizationStrategy(new TransactionManagerAccess() {
		@Override
		public TransactionManager getTransactionManager() {
			return retrieveTransactionManager();
		}
	});

	@Override
	public TransactionManager retrieveTransactionManager() {
		return manager.get();
	}

	@Override
	public UserTransaction retrieveUserTransaction() {
		return userTransaction.get();
	}

	@Override
	public Object getTransactionIdentifier(Transaction transaction) {
		return transaction;
	}

	@Override
	public boolean canRegisterSynchronization() {
		return synchronizationStrategy.canRegisterSynchronization();
	}

	@Override
	public void registerSynchronization(Synchronization synchronization) {
		synchronizationStrategy.registerSynchronization(synchronization);
	}

	@Override
	public int getCurrentStatus() throws SystemException {
		return manager.get().getStatus();
	}
}
