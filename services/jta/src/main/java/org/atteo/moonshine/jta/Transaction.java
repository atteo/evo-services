/*
 * Copyright 2011 Atteo.
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
package org.atteo.moonshine.jta;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class Transaction {
	public static interface Runnable {
		void run();
	}

	public static interface ThrowingRunnable<E extends Throwable> {
		void run() throws E;
	}

	public static interface ReturningRunnable<T, E extends Throwable> {
		T run() throws E;
	}

	private static final InheritableThreadLocal<Provider<UserTransaction>> userTransactionProviders
			= new InheritableThreadLocal<>();

	@Inject
	private static void injectTransactionProvider(Provider<UserTransaction> provider) {
		userTransactionProviders.set(provider);
	}

	public static void require(final Runnable runnable) {
		require(new ReturningRunnable<Void, RuntimeException>() {
			@Override
			public Void run() {
				runnable.run();
				return null;
			}
		});
	}

	public static <E extends Throwable> void require(final ThrowingRunnable<E> runnable) throws E {
		require(new ReturningRunnable<Void, E>() {
			@Override
			public Void run() throws E {
				runnable.run();
				return null;
			}
		});
	}

	public static <T, E extends Throwable> T require(ReturningRunnable<T, E> runnable) throws E {
		checkNotNull(userTransactionProviders.get(), "Transactions not supported. You need to add <transactional/>"
				+ " to your configuration file.");
		UserTransaction userTransaction = userTransactionProviders.get().get();

		return require(userTransaction, runnable);
	}

	private static <T, E extends Throwable> T require(UserTransaction userTransaction,
			ReturningRunnable<T, E> runnable) throws E {
		boolean myTransaction = false;

		try {
			if (userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION) {
				userTransaction.begin();
				myTransaction = true;
			}

			try {
				return runnable.run();
			} catch (RuntimeException e) {
				userTransaction.setRollbackOnly();
				throw e;
			} finally {
				if (myTransaction) {
					if (userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
						userTransaction.rollback();
					} else {
						userTransaction.commit();
					}
				}
			}
		} catch (SystemException | NotSupportedException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException e) {
			throw new RuntimeException(e);
		}
	}
}
