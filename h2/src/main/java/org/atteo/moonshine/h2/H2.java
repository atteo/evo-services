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
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.database.DatabaseService;
import org.atteo.moonshine.jta.JtaDataSourceWrapper;
import org.atteo.moonshine.jta.PoolOptions;
import org.h2.jdbcx.JdbcDataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;

@XmlRootElement(name = "h2")
public class H2 extends DatabaseService {
	@XmlElement
	@XmlDefaultValue("jdbc:h2:${dataHome}/database;AUTO_SERVER=TRUE")
	private String url;

	@XmlElement
	private String username = "";

	@XmlElement
	private String password = "";

	@XmlElement
	private PoolOptions pool;

	@Inject
	private JtaDataSourceWrapper wrapper;

	private DataSource dataSource;

	private class DataSourceProvider implements Provider<DataSource> {
		@Inject
		private JtaDataSourceWrapper wrapper;

		@Override
		public DataSource get() {
			final JdbcDataSource xaDataSource = new JdbcDataSource();
			xaDataSource.setURL(url);
			xaDataSource.setUser(username);
			xaDataSource.setPassword(password);
			String name = "defaultDataSource";
			if (getId() != null) {
				name = getId();
			}
			dataSource = wrapper.wrap(name, xaDataSource, pool);
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
	public void deconfigure() {
		if (wrapper != null) {
			wrapper.close(dataSource);
		}
	}
}
