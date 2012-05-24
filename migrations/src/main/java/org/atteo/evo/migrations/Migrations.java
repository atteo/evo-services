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
