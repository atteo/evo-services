package org.atteo.evo.migrations;

import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class Migrations {
	private DataSource dataSource;

	Migrations(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void migrate(String changelog) {
		migrate(changelog, null);
	}

	public void migrate(String changelog, String contexts) {
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(this.getClass()
				.getClassLoader());

		DatabaseConnection databaseConnection = null;

		try {
			databaseConnection = new JdbcConnection(dataSource.getConnection());
			Liquibase liquibase = new Liquibase(changelog, resourceAccessor, databaseConnection);
			liquibase.update(contexts);
		} catch (LiquibaseException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (databaseConnection != null && !databaseConnection.isClosed())
					databaseConnection.close();
			} catch (DatabaseException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void dropAll() {
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(this.getClass()
				.getClassLoader());

		DatabaseConnection databaseConnection = null;

		try {
			databaseConnection = new JdbcConnection(dataSource.getConnection());
			Liquibase liquibase = new Liquibase(null, resourceAccessor, databaseConnection);
			liquibase.dropAll();
		} catch (LiquibaseException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (databaseConnection != null && !databaseConnection.isClosed())
					databaseConnection.close();
			} catch (DatabaseException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
