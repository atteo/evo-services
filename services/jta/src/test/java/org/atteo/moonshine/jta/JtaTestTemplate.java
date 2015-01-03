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
package org.atteo.moonshine.jta;

import javax.inject.Inject;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Test;

public class JtaTestTemplate extends MoonshineTest {
	@Inject
	private UserTransaction userTransaction;

	@Inject
	private JtaDataSourceWrapper dataSourceWrapper;

	@Inject
	private JtaConnectionFactoryWrapper connectionFactoryWrapper;

	@Test
	public void shouldProvideInjections() throws SystemException {
		assertThat(userTransaction.getStatus()).isEqualTo(Status.STATUS_NO_TRANSACTION);
		assertThat(dataSourceWrapper).isNotNull();
		assertThat(connectionFactoryWrapper).isNotNull();
	}

	@Test
	public void shouldStartTransaction() throws NotSupportedException, SystemException {
		userTransaction.begin();
		assertThat(userTransaction.getStatus()).isEqualTo(Status.STATUS_ACTIVE);

		userTransaction.rollback();
		assertThat(userTransaction.getStatus()).isEqualTo(Status.STATUS_NO_TRANSACTION);
	}
}
