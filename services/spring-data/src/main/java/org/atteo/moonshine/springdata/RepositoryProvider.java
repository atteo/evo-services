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

import org.atteo.moonshine.jta.Transaction;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.google.inject.Provider;

/**
 * Provides repository of given class.
 *
 * <p>
 * Requires {@link RepositoryFactorySupport}.
 * </p>
 */
public class RepositoryProvider<T> implements Provider<T> {
	@Inject
	private RepositoryFactorySupport factory;

	private Class<T> klass;

	public RepositoryProvider(Class<T> klass) {
		this.klass = klass;
	}

	@Override
	public T get() {
		return Transaction.require(() -> factory.getRepository(klass));
	}

}
