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

package org.atteo.moonshine.jta;

import javax.inject.Singleton;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

@XmlRootElement(name = "jta-mock")
@Singleton
public class JtaMock extends JtaService {
	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(UserTransaction.class).toInstance(new UserTransaction() {
                    int status = Status.STATUS_NO_TRANSACTION;

					@Override
					public void begin() throws NotSupportedException, SystemException {
                        status = Status.STATUS_ACTIVE;
					}

					@Override
					public void commit() throws RollbackException, HeuristicMixedException,
                            HeuristicRollbackException, SecurityException, IllegalStateException,
                            SystemException {
                        status = Status.STATUS_NO_TRANSACTION;
					}

					@Override
					public void rollback() throws IllegalStateException, SecurityException, SystemException {
                        status = Status.STATUS_NO_TRANSACTION;
					}

					@Override
					public void setRollbackOnly() throws IllegalStateException, SystemException {
                        status = Status.STATUS_MARKED_ROLLBACK;
					}

					@Override
					public int getStatus() throws SystemException {
                        return status;
					}

					@Override
					public void setTransactionTimeout(int seconds) throws SystemException {
					}
				});
			}
		};
	}
}
