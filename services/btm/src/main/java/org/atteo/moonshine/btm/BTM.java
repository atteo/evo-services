/*
 * Copyright 2014 Atteo.
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
package org.atteo.moonshine.btm;

import java.lang.management.ManagementFactory;

import javax.inject.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.jta.JtaConnectionFactoryWrapper;
import org.atteo.moonshine.jta.JtaDataSourceWrapper;
import org.atteo.moonshine.jta.JtaService;
import org.atteo.moonshine.jta.PoolOptions;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import bitronix.tm.resource.jms.PoolingConnectionFactory;

/**
 * Bitronix JTA transaction manager.
 */
@XmlRootElement(name = "btm")
public class BTM extends JtaService {
	@XmlElement
	@XmlDefaultValue("${dataHome}/jta/logs/btm1.log")
	private String logPart1FileName;

	@XmlElement
	@XmlDefaultValue("${dataHome}/jta/logs/btm2.log")
	private String logPart2FileName;

	/**
	 * The default timeout (in seconds) that is set for transactions when no timeout is specified.
	 */
	@XmlElement
	@XmlDefaultValue("60")
	private Integer transactionTimeout;

	private BitronixTransactionManager transactionManager;

	private static class BitronixDataSourceWrapper implements JtaDataSourceWrapper {
		@Override
		public DataSource wrap(String name, XADataSource xaDataSource, PoolOptions poolOptions, String testQuery) {
			PoolingDataSource dataSource = new PoolingDataSource();

			dataSource.setUniqueName(name);
			dataSource.setXaDataSource(xaDataSource);
			if (poolOptions == null) {
				poolOptions = new PoolOptions();
			}
			if (poolOptions.getMinPoolSize() != null) {
				dataSource.setMinPoolSize(poolOptions.getMinPoolSize());
			}
			if (poolOptions.getMaxPoolSize() != null) {
				dataSource.setMaxPoolSize(poolOptions.getMaxPoolSize());
			}
			if (poolOptions.getMaxIdleTime() != null) {
				dataSource.setMaxIdleTime(poolOptions.getMaxIdleTime());
			}
			if (poolOptions.getMaxLifeTime() != null) {
				dataSource.setMaxLifeTime(poolOptions.getMaxLifeTime());
			}
			// TODO: poolOptions.getReapTimeout();
			dataSource.setTestQuery(testQuery);

			// DDL statement cannot be executed within XA transaction
			// So we need to allow non-XA statements during
			dataSource.setAllowLocalTransactions(true);

			return dataSource;
		}

		@Override
		public void close(DataSource dataSource) {
			((PoolingDataSource)dataSource).close();
		}
	}

	private static class BitronixFactoryConnectionWrapper implements JtaConnectionFactoryWrapper {
		@Override
		public ConnectionFactory wrap(String name, XAConnectionFactory xaFactory, PoolOptions poolOptions) {
			PoolingConnectionFactory connectionFactory = new PoolingConnectionFactory();
			connectionFactory.setUniqueName(name);
			connectionFactory.setXaConnectionFactory(xaFactory);

			if (poolOptions == null) {
				poolOptions = new PoolOptions();
			}
			if (poolOptions.getMinPoolSize() != null) {
				connectionFactory.setMinPoolSize(poolOptions.getMinPoolSize());
			}
			if (poolOptions.getMaxPoolSize() != null) {
				connectionFactory.setMaxPoolSize(poolOptions.getMaxPoolSize());
			}
			if (poolOptions.getMaxIdleTime() != null) {
				connectionFactory.setMaxIdleTime(poolOptions.getMaxIdleTime());
			}
			if (poolOptions.getMaxLifeTime() != null) {
				connectionFactory.setMaxLifeTime(poolOptions.getMaxLifeTime());
			}
			// TODO: poolOptions.getReapTimeout();

			return connectionFactory;
		}

		@Override
		public void close(ConnectionFactory connectionFactory) {
			((PoolingConnectionFactory)connectionFactory).close();
		}
	}

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				configureCommon(binder());

				LocalBitronixContext.initNewContext();

				Configuration configuration = TransactionManagerServices.getConfiguration();
				configuration.setLogPart1Filename(logPart1FileName);
				configuration.setLogPart2Filename(logPart2FileName);
				configuration.setDisableJmx(true);
				configuration.setServerId(ManagementFactory.getRuntimeMXBean().getName());
				configuration.setDefaultTransactionTimeout(transactionTimeout);

				transactionManager = TransactionManagerServices.getTransactionManager();

				bind(TransactionManager.class).toInstance(transactionManager);
				bind(UserTransaction.class).toInstance(transactionManager);
				bind(JtaDataSourceWrapper.class).to(BitronixDataSourceWrapper.class).in(Singleton.class);
				bind(JtaConnectionFactoryWrapper.class).to(BitronixFactoryConnectionWrapper.class).in(Singleton.class);
			}
		};
	}

	@Override
	public void close() {
		transactionManager.shutdown();
		transactionManager = null;
	}
}
