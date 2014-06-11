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

package org.atteo.moonshine.postgresql;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.database.DatabaseService;
import org.atteo.moonshine.jta.JtaDataSourceWrapper;
import org.atteo.moonshine.jta.JtaService;
import org.atteo.moonshine.jta.PoolOptions;
import org.atteo.moonshine.services.ImportService;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.common.BaseDataSource;
import org.postgresql.xa.PGXADataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;

/**
 * Connects to the PostgreSQL database.
 */
@XmlRootElement(name = "postgresql")
public class PostgreSQLService extends DatabaseService {
	@ImportService
	@XmlIDREF
	@XmlElement
	private JtaService jta;

	/**
	 * Sets the name of the PostgreSQL database, running on the server identified by the serverName property.
	 */
	@XmlElement(required = true)
	private String databaseName;

	/**
	 * Sets the name of the host the PostgreSQL database is running on. The default value is localhost.
	 */
	@XmlElement
	private String serverName;

	/**
	 * Sets the port which the PostgreSQL server is listening on for TCP/IP connections.
	 */
	@XmlElement
	private Integer portNumber;

	/**
	 * Database user.
	 */
	@XmlElement
	private String user = "";

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

		private void configure(BaseDataSource dataSource) {
			dataSource.setDatabaseName(databaseName);
			if (serverName != null) {
				dataSource.setServerName(serverName);
			}
			if (portNumber != null) {
				dataSource.setPortNumber(portNumber);
			}

			if (user != null) {
				dataSource.setUser(user);
			}

			if (password != null) {
				dataSource.setPassword(password);
			}
		}

		@Override
		public DataSource get() {
			final PGSimpleDataSource migrationDataSource = new PGSimpleDataSource();
			configure(migrationDataSource);
			executeMigrations(migrationDataSource);

			final PGXADataSource xaDataSource = new PGXADataSource();
			configure(xaDataSource);

			String name = "defaultDataSource";
			if (getId() != null) {
				name = getId();
			}
			dataSource = wrapper.wrap(name, xaDataSource, pool, testQuery);
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
