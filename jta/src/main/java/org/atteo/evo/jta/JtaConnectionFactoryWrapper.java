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
package org.atteo.evo.jta;

import javax.annotation.Nullable;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;


/**
 * Wraps {@link XAConnectionFactory} into {@link ConnectionFactory} adding transaction support.
 */
public interface JtaConnectionFactoryWrapper {
	/**
	 * Wrap given {@link XAConnectionFactory} into {@link ConnectionFactory} adding transaction support.
	 * @param name unique name for the returned {@link ConnectionFactory}
	 * @param xaFactory {@link ConnectionFactory} to wrap
	 * @param poolOptions options for the connection pool
	 * @return wrapped {@link ConnectionFactory}
	 */
	ConnectionFactory wrap(String name, XAConnectionFactory xaFactory,
			@Nullable PoolOptions poolOptions);

	/**
	 * Close previously wrapped connection factory.
	 * @param connectionFactory connection factory to close
	 */
	void close(ConnectionFactory connectionFactory);
}
