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
package org.atteo.moonshine.h2;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.database.DatabaseService;
import org.atteo.moonshine.jta.JtaDataSourceWrapper;
import org.atteo.moonshine.jta.JtaService;
import org.atteo.moonshine.jta.PoolOptions;
import org.atteo.moonshine.services.ImportService;
import org.h2.jdbcx.JdbcDataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;

/**
 * Starts H2 database
 */
@XmlRootElement(name = "h2")
public class H2 extends DatabaseService {
	@XmlIDREF
	@XmlElement
	@ImportService
	private JtaService jtaService;

	/**
	 * Database URL.
	 * <p>
	 * By default file database is used. For in-memory database set url to:
	 * <pre>
	 * jdbc:h2:mem:;DB_CLOSE_DELAY=-1
	 * </pre>
	 * </p>
	 * @see <a href="http://www.h2database.com/html/features.html#database_url">H2 documentation</a>
	 */
	@XmlElement
	@XmlDefaultValue("jdbc:h2:${dataHome}/database")
	private String url;

	/**
	 * Database user.
	 */
	@XmlElement
	private String username = "";

	/**
	 * Database password.
	 */
	@XmlElement
	private String password = "";

	/**
	 * Connection pool options.
	 */
	@XmlElement
	private PoolOptions pool;

	@XmlElement
	private String testQuery = "select 1";

	@Inject
	private JtaDataSourceWrapper wrapper;

	private DataSource dataSource;

	private class DataSourceProvider implements Provider<DataSource> {
		@Inject
		private JtaDataSourceWrapper wrapper;

		@Override
		public DataSource get() {
			final JdbcDataSource xaDataSource = new JdbcDataSource();
			xaDataSource.setURL(url + ";DB_CLOSE_ON_EXIT=FALSE");
			xaDataSource.setUser(username);
			xaDataSource.setPassword(password);
			String name = Thread.currentThread().getName();
			if (getId() == null) {
				name += "-defaultDataSource";
			} else {
				name = getId();
			}
			xaDataSource.setDescription(name);
			dataSource = wrapper.wrap(name, xaDataSource, pool, testQuery);
			executeMigrations(dataSource);
			return dataSource;
		}
	}

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(DataSource.class).toProvider(new DataSourceProvider()).in(Scopes.SINGLETON);
			}
		};
	}

	@Override
	public void close() {
		if (dataSource != null) {
			wrapper.close(dataSource);
		}
	}
}
