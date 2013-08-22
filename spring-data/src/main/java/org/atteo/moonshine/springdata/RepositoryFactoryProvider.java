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
package org.atteo.moonshine.springdata;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.atteo.moonshine.jta.Transaction;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.google.inject.Provider;

/**
 * Provides {@link RepositoryFactorySupport}.
 * <p>
 * Needs {@link EntityManager}.
 * </p>
 */
public class RepositoryFactoryProvider implements Provider<RepositoryFactorySupport> {
	/*
		Let's inject DatabaseInitializer before injecting EntityManager, so the provider registered
		for DatabaseInitializer can initialize the database before EntityManager is created.
	*/
	@Inject
	private DatabaseInitializer databaseInitializer;

	@Inject
	private EntityManager manager;

	@Override
	public RepositoryFactorySupport get() {
		return Transaction.require(new Transaction.ReturningRunnable<RepositoryFactorySupport, RuntimeException>() {
			@Override
			public RepositoryFactorySupport run() throws RuntimeException {
				return new JpaRepositoryFactory(manager);
			}
		});
	}
}
