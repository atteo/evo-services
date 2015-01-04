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

import javax.inject.Inject;
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

import org.atteo.config.XmlDefaultValue;
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
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;

/**
 * Atomikos JTA implementation.
 * <p>
 * Note: Atomikos can be started only once in the same JVM. This is because of limitation
 * of the Atomikos itself. (It used static fields in {@link com.atomikos.icatch.system.Configuration} class.)
 * You will get {@link IllegalStateException} when started the second time.
 * </p>
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

	/**
	 * The default timeout (in seconds) that is set for transactions when no timeout is specified.
	 */
	@XmlElement
	@XmlDefaultValue("60")
	private Integer transactionTimeout;

	private UserTransactionManager manager;
	private UserTransactionServiceImp service;
	private static boolean initialized = false;

	private synchronized static void turnOn() {
		if (initialized) {
			throw new IllegalStateException("Atomikos cannot be started two times in the same JVM. Use BTM instead.");
		}
		initialized = true;
	}

	private synchronized static void turnOff() {
		initialized = false;
	}

	public class ManagerProvider implements Provider<UserTransactionManager> {
		@Override
		public UserTransactionManager get() {
			turnOn();
			System.setProperty(UserTransactionServiceImp.NO_FILE_PROPERTY_NAME, "true");
			System.setProperty(UserTransactionServiceImp.HIDE_INIT_FILE_PATH_PROPERTY_NAME,
					"true");
			System.setProperty("com.atomikos.icatch.service",
					UserTransactionServiceFactory.class.getCanonicalName());

			Properties properties = new Properties();
			properties.setProperty(AbstractUserTransactionServiceFactory.MAX_ACTIVES_PROPERTY_NAME,
					Integer.toString(maxActiveTransactions));
			String tmName = "TM_" + ManagementFactory.getRuntimeMXBean().getName();
			if (tmName.length() > 30) {
				tmName = tmName.substring(0, 30);
			}
			properties.setProperty(AbstractUserTransactionServiceFactory.TM_UNIQUE_NAME_PROPERTY_NAME, tmName);
			properties.setProperty(AbstractUserTransactionServiceFactory.LOG_BASE_NAME_PROPERTY_NAME,
					"log_");
			properties.setProperty(AbstractUserTransactionServiceFactory.LOG_BASE_DIR_PROPERTY_NAME,
					logDirectory);
			properties.setProperty(AbstractUserTransactionServiceFactory.OUTPUT_DIR_PROPERTY_NAME,
					consoleOutputDirectory);
			properties.setProperty(AbstractUserTransactionServiceFactory.THREADED_2PC_PROPERTY_NAME,
					"false");
			properties.setProperty(AbstractUserTransactionServiceFactory.DEFAULT_JTA_TIMEOUT_PROPERTY_NAME,
					Integer.toString(transactionTimeout * 1000));
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
			return manager;
		}
	}

	private static class AtomikosDataSourceWrapper implements JtaDataSourceWrapper {
		@Inject
		UserTransactionManager userTransactionManager;

		@Override
		public DataSource wrap(String name, XADataSource xaDataSource, PoolOptions poolOptions, String testQuery) {
			AtomikosDataSourceBean wrapped = new AtomikosDataSourceBean();
			wrapped.setXaDataSource(xaDataSource);
			wrapped.setUniqueResourceName(name);

			if (poolOptions == null) {
				poolOptions = new PoolOptions();
			}
			if (poolOptions.getMaxLifeTime() != null && poolOptions.getMaxLifeTime() != 0) {
				wrapped.setMaxLifetime(poolOptions.getMaxLifeTime());
			} else {
				// test query is only needed when we don't know how long Atomikos can keep connections in the pool
				wrapped.setTestQuery(testQuery);
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
		return new PrivateModule() {
			@Override
			protected void configure() {
				configureCommon(binder());

				bind(UserTransactionManager.class).toProvider(new ManagerProvider()).in(Singleton.class);
				bind(TransactionManager.class).to(UserTransactionManager.class);
				expose(TransactionManager.class);
				bind(UserTransaction.class).to(UserTransactionManager.class);
				expose(UserTransaction.class);
				bind(JtaDataSourceWrapper.class).to(AtomikosDataSourceWrapper.class).in(Singleton.class);
				expose(JtaDataSourceWrapper.class);
				bind(JtaConnectionFactoryWrapper.class).to(AtomikosConnectionFactoryWrapper.class).in(Singleton.class);
				expose(JtaConnectionFactoryWrapper.class);
			}
		};
	}

	@Override
	public void close() {
		if (manager != null) {
			manager.close();
		}
		if (service != null) {
			service.shutdownWait();
		}
		turnOff();
	}
}
