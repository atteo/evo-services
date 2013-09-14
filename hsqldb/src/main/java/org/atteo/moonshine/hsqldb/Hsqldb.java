package org.atteo.moonshine.hsqldb;

import java.sql.SQLException;

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
import org.hsqldb.jdbc.pool.JDBCXADataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Starts HSQLDB database.
 */
@XmlRootElement(name = "hsqldb")
public class Hsqldb extends DatabaseService {
	@XmlIDREF
	@XmlElement
	@ImportService
	private JtaService jtaService;
	@XmlElement
	private String url = "jdbc:hsqldb:mem:mymemdb";

	@XmlElement
	private PoolOptions pool;

	@Inject
	private JtaDataSourceWrapper wrapper;

	private DataSource dataSource;

	@Singleton
	private class DataSourceProvider implements Provider<DataSource> {
		@Inject
		private JtaDataSourceWrapper wrapper;

		@Override
		public DataSource get() {
			final JDBCXADataSource xaDataSource;
			try {
				xaDataSource = new JDBCXADataSource();
			} catch (SQLException e) {
				throw new RuntimeException("Cannot create data source", e);
			}
			String name = "defaultDataSource";
			if (getId() != null) {
				name = getId();
			}
			xaDataSource.setUrl(url);
			dataSource = wrapper.wrap(name, xaDataSource, pool, "VALUES 1");

			executeMigrations(dataSource);
			return dataSource;
		}
	}

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			public void configure() {
				bind(DataSource.class).toProvider(new DataSourceProvider()).in(Singleton.class);
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
