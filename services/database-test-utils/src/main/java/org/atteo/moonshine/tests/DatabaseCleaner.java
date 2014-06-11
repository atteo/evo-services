/*
 * Copyright 2011 Atteo.
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
package org.atteo.moonshine.tests;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.atteo.moonshine.database.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatabaseCleaner {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseCleaner.class);

	private final DataSource dataSource;

	private final DatabaseService database;

	public DatabaseCleaner(DataSource dataSource, DatabaseService database) {
		this.dataSource = dataSource;
		this.database = database;
	}

	/**
	 * Restore the database to its pristine state (after all migrations have run).
	 */
	public void reset() {
		dropTables();
		database.executeMigrations(dataSource);
	}

	/**
	 * Clean all database tables.
	 */
	public void clean() {
		logger.debug("Clearing database");
		try (Connection connection = dataSource.getConnection()) {

			List<String> tables = analyseDatabase(connection);

			clearTables(connection, tables);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Drop all database tables.
	 */
	public void dropTables() {
		try (Connection connection = dataSource.getConnection()) {
			List<String> tables = analyseDatabase(connection);

			dropTables(connection, tables);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	private List<String> analyseDatabase(Connection connection) {
		try {
			List<String> tables = new ArrayList<>();

			DatabaseMetaData metaData = connection.getMetaData();

			try (ResultSet result = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
				while (result.next()) {
					String tableName = result.getString("TABLE_NAME");
					tables.add(tableName);
				}
			}

			return tables;
		} catch (SQLException e) {
			throw new RuntimeException("An exception occurred while trying to analyse the database.", e);
		}
	}

	private void clearTables(Connection connection, List<String> tables) {
		for (String table : tables) {
			if (!table.equals("DATABASECHANGELOG") && !table.equals("DATABASECHANGELOGLOCK")) {
				clearSingleTable(connection, table);
			}
		}

	}


	private void clearSingleTable(Connection connection, String tableName) {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("DELETE FROM " + tableName);
		} catch (SQLException ex) {
			throw new RuntimeException("Can't read table contents from table ".concat(tableName), ex);
		}
	}

	private void dropTables(Connection connection, List<String> tables) {
		for (String table : tables) {
			dropTable(connection, table);
		}
	}

	private void dropTable(Connection connection, String tableName) {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("DROP TABLE " + tableName);
		} catch (SQLException ex) {
			throw new RuntimeException("Can't read table contents from table ".concat(tableName), ex);
		}
	}
}
