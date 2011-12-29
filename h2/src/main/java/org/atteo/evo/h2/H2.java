package org.atteo.evo.h2;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.evo.database.DatabaseService;
import org.atteo.evo.jta.JtaDataSourceWrapper;
import org.atteo.evo.jta.PoolOptions;
import org.h2.jdbcx.JdbcDataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

@XmlRootElement(name = "h2")
public class H2 extends DatabaseService {
	@XmlElement
	@XmlDefaultValue("jdbc:h2:${applicationHome}/database;AUTO_SERVER=TRUE")
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
				String id = getId();
				if (id == null) {
					bind(DataSource.class).toProvider(new DataSourceProvider())
							.in(Scopes.SINGLETON);
				} else {
					bind(Key.get(DataSource.class, Names.named(id))).toProvider(
							new DataSourceProvider()).in(Scopes.SINGLETON);
				}
			}
		};
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
		wrapper.close(dataSource);
	}
}
