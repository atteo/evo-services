/*
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
package org.atteo.moonshine.atomikos;

import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.inject.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.jta.JtaConnectionFactoryWrapper;
import org.atteo.moonshine.jta.JtaDataSourceWrapper;
import org.atteo.moonshine.jta.JtaService;
import org.atteo.moonshine.jta.PoolOptions;

import com.atomikos.icatch.SysException;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.config.imp.AbstractUserTransactionServiceFactory;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.icatch.standalone.UserTransactionServiceFactory;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Atomikos JTA implementation.
 */
@XmlRootElement(name = "atomikos")
public class Atomikos extends JtaService {
	/**
	 * Specifies the maximum number of active transactions.
	 * <p>
	 * A negative value means infinite amount. You will get an IllegalStateException
	 * with error message "Max number of active transactions reached" if you call
	 * {@link UserTransaction#begin()} while there are already n concurrent transactions running,
	 * n being this value.
	 * </p>
	 */
	@XmlElement
	private Integer maxActiveTransactions = -1;

	@XmlElement
	@XmlDefaultValue("${dataHome}/atomikos/logs")
	private String logDirectory;

	@XmlElement
	@XmlDefaultValue("${dataHome}/atomikos/")
	private String consoleOutputDirectory;

	@XmlElement
	@XmlDefaultValue("60")
	private Integer transactionTimeout;

	private UserTransactionManager manager;
	private UserTransactionServiceImp service;

	@Singleton
	private static class AtomikosDataSourceWrapper implements JtaDataSourceWrapper {
		@Override
		public DataSource wrap(String name, XADataSource xaDataSource, PoolOptions poolOptions, String testQuery) {
			AtomikosDataSourceBean wrapped = new AtomikosDataSourceBean();
			wrapped.setXaDataSource(xaDataSource);
			wrapped.setUniqueResourceName(name);
			wrapped.setTestQuery(testQuery);
			if (poolOptions == null) {
				return wrapped;
			}

			if (poolOptions.getMinPoolSize() != null) {
				wrapped.setMinPoolSize(poolOptions.getMinPoolSize());
			}
			if (poolOptions.getMaxPoolSize() != null) {
				wrapped.setMaxPoolSize(poolOptions.getMaxPoolSize());
			}
			if (poolOptions.getMaxIdleTime() != null) {
				wrapped.setMaxIdleTime(poolOptions.getMaxIdleTime());
			}

			if (poolOptions.getReapTimeout() != null) {
				wrapped.setReapTimeout(poolOptions.getReapTimeout());
			}
			return wrapped;
		}

		@Override
		public void close(DataSource dataSource) {
			((AtomikosDataSourceBean) dataSource).close();
		}
	}

	@Singleton
	private static class AtomikosConnectionFactoryWrapper implements JtaConnectionFactoryWrapper {
		@Override
		public ConnectionFactory wrap(String name, XAConnectionFactory xaFactory,
				PoolOptions poolOptions) {
			AtomikosConnectionFactoryBean wrapped = new AtomikosConnectionFactoryBean();
			wrapped.setXaConnectionFactory(xaFactory);
			wrapped.setUniqueResourceName(name);
			if (poolOptions == null) {
				return wrapped;
			}

			if (poolOptions.getMinPoolSize() != null) {
				wrapped.setMinPoolSize(poolOptions.getMinPoolSize());
			}
			if (poolOptions.getMaxPoolSize() != null) {
				wrapped.setMaxPoolSize(poolOptions.getMaxPoolSize());
			}
			if (poolOptions.getMaxIdleTime() != null) {
				wrapped.setMaxIdleTime(poolOptions.getMaxIdleTime());
			}

			if (poolOptions.getReapTimeout() != null) {
				wrapped.setReapTimeout(poolOptions.getReapTimeout());
			}
			return wrapped;
		}

		@Override
		public void close(ConnectionFactory connectionFactory) {
			((AtomikosConnectionFactoryBean) connectionFactory).close();
		}
	}

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				System.setProperty(UserTransactionServiceImp.NO_FILE_PROPERTY_NAME, "true");
				System.setProperty(UserTransactionServiceImp.HIDE_INIT_FILE_PATH_PROPERTY_NAME,
						"true");
				System.setProperty("com.atomikos.icatch.service",
						UserTransactionServiceFactory.class.getCanonicalName());

				Properties properties = new Properties();
				properties.setProperty(AbstractUserTransactionServiceFactory.MAX_ACTIVES_PROPERTY_NAME,
						Integer.toString(maxActiveTransactions));
				properties.setProperty(AbstractUserTransactionServiceFactory.TM_UNIQUE_NAME_PROPERTY_NAME,
						"TM_" + ManagementFactory.getRuntimeMXBean().getName());
				properties.setProperty(AbstractUserTransactionServiceFactory.LOG_BASE_NAME_PROPERTY_NAME,
						"log_");
				properties.setProperty(AbstractUserTransactionServiceFactory.LOG_BASE_DIR_PROPERTY_NAME,
						logDirectory);
				properties.setProperty(AbstractUserTransactionServiceFactory.OUTPUT_DIR_PROPERTY_NAME,
						consoleOutputDirectory);
				properties.setProperty(AbstractUserTransactionServiceFactory.THREADED_2PC_PROPERTY_NAME,
						"false");
				properties.setProperty(AbstractUserTransactionServiceFactory.DEFAULT_JTA_TIMEOUT_PROPERTY_NAME,
						Integer.toString(transactionTimeout * 60));
				service = new UserTransactionServiceImp(properties);
				try {
					service.init();
				} catch (SysException e) {
					throw new RuntimeException(e.getErrors().pop().toString(), e);
				}

				manager = new UserTransactionManager();
				manager.setStartupTransactionService(false);
				try {
					manager.init();
				} catch (SystemException e) {
					throw new RuntimeException(e);
				}
				bind(TransactionManager.class).toInstance(manager);
				bind(UserTransaction.class).toInstance(manager);
				bind(JtaDataSourceWrapper.class).to(AtomikosDataSourceWrapper.class);
				bind(JtaConnectionFactoryWrapper.class).to(AtomikosConnectionFactoryWrapper.class);
			}
		};
	}

	@Override
	public void deconfigure() {
		manager.close();
		service.shutdownWait();
	}
}
