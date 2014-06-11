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
package org.atteo.moonshine.liquibase;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * Liquibase facade for migrations execution.
 * <p>
 * Usage:
 * <pre>
 * &#64;ImportService
 * private DatabaseService database;
 *
 * ...
 *
 * database.registerMigration(new DatabaseMigration() {
 *     &#64;Override
 *     public void execute(DataSource dataSource) {
 *         new LiquibaseFacade(datasource).migrate("/migrations/migration01.xml");
 *     }
 * }
 * </pre>
 * </p>
 */
public class LiquibaseFacade {
	private static final String BEFORE_LAST_UPDATE = "BEFORE_LAST_UPDATE";

	private final DataSource dataSource;

	@Inject
	public LiquibaseFacade(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Migrate using given migration.
	 * @param changelog resource with the migration
	 */
	public void migrate(String changelog) {
		migrate(changelog, null);
	}

	/**
	 * Migrate using given migration.
	 * @param changelog resource with the migration
	 * @param contexts contexts, see {@link Liquibase#update(String)}.
	 */
	public void migrate(String changelog, String contexts) {
		migrate(changelog, contexts, null);
	}

	/**
	 * Migrate using given migration.
	 * @param changelog resource with the migration
	 * @param contexts contexts, see {@link Liquibase#update(String)}.
	 * @param changelogParameters changelog parameters, can be null
	 */
	public void migrate(String changelog, String contexts, Map<String, Object> changelogParameters) {
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

		DatabaseConnection databaseConnection = null;
		changelog = normalizeName(changelog);

		try {
			databaseConnection = new JdbcConnection(dataSource.getConnection());
			Liquibase liquibase = new Liquibase(changelog, resourceAccessor, databaseConnection);
			liquibase.tag(BEFORE_LAST_UPDATE);

			if (changelogParameters != null) {
				for (Entry<String, Object> entry : changelogParameters.entrySet()) {
					liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
				}
			}

			liquibase.update(contexts);
		} catch (LiquibaseException | SQLException e) {
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

	/**
	 * Rollbacks last update.
	 * @param changelog resource with the migration
	 */
	public void rollbackLastUpdate(String changelog) {
		rollbackLastUpdate(changelog, null);
	}

	/**
	 * Rollbacks last update.
	 * @param changelog resource with the migration
	 * @param contexts contexts, see {@link Liquibase#update(String)}.
	 */
	public void rollbackLastUpdate(String changelog, String contexts) {
		rollbackLastUpdate(changelog, contexts, null) ;
	}

	/**
	 * Rollbacks given changelog to given tag.
	 * @param changelog resource with the migration
	 * @param contexts contexts, see {@link Liquibase#update(String)}.
	 * @param changelogParameters changelogParameters
	 */
	public void rollbackLastUpdate(String changelog, String contexts, Map<String, Object> changelogParameters) {
		rollback(changelog, contexts, changelogParameters, BEFORE_LAST_UPDATE);
	}

	/**
	 * Rollbacks given changelog to given tag.
	 * @param changelog resource with the migration
	 * @param contexts contexts, see {@link Liquibase#update(String)}.
	 * @param changelogParameters changelogParameters
	 * @param tag tag to rollback to
	 */
	private void rollback(String changelog, String contexts, Map<String, Object> changelogParameters, String tag) {
		changelog = normalizeName(changelog);

		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

		DatabaseConnection databaseConnection = null;

		try {
			databaseConnection = new JdbcConnection(dataSource.getConnection());
			Liquibase liquibase = new Liquibase(changelog, resourceAccessor, databaseConnection);

			if (changelogParameters != null) {
				for (Entry<String, Object> entry : changelogParameters.entrySet()) {
					liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
				}
			}

			liquibase.rollback(tag, contexts);
		} catch (LiquibaseException | SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (databaseConnection != null && !databaseConnection.isClosed()) {
					databaseConnection.close();
				}
			} catch (DatabaseException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void dropAll() {
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

		DatabaseConnection databaseConnection = null;

		try {
			databaseConnection = new JdbcConnection(dataSource.getConnection());
			Liquibase liquibase = new Liquibase(null, resourceAccessor, databaseConnection);
			liquibase.dropAll();
		} catch (LiquibaseException | SQLException e) {
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

	private String normalizeName(String changelog) {
		if (changelog.startsWith("/") ){
			changelog = changelog.substring(1);
		}
		return changelog;
	}
}
