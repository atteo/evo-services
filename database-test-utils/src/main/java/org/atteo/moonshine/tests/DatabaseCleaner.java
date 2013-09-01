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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseCleaner {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseCleaner.class);

	public static void clean(DataSource dataSource) {
		logger.debug("Clearing database");
		try (Connection connection = dataSource.getConnection()) {

			List<String> tables = analyseDatabase(connection);

			clearTables(connection, tables);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<String> analyseDatabase(Connection connection) {
		try {
			List<String> tables = new ArrayList<>();

			DatabaseMetaData metaData = connection.getMetaData();

			try (ResultSet result = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
				while (result.next()) {
					String tableName = result.getString("TABLE_NAME");
					if (tableName.equals("DATABASECHANGELOG") || tableName.equals("DATABASECHANGELOGLOCK")) {
						dropTable(connection, tableName);
					} else {
						tables.add(tableName);
					}
				}
			}

			return tables;
		} catch (SQLException e) {
			throw new RuntimeException("An exception occurred while trying to analyse the database.", e);
		}
	}

	private static void clearTables(Connection connection, List<String> tables) {
		for (String table : tables) {
			clearSingleTable(connection, table);
		}
	}

	private static void dropTable(Connection connection, String tableName) {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("DROP TABLE " + tableName);
		} catch (SQLException ex) {
			throw new RuntimeException("Can't read table contents from table ".concat(tableName), ex);
		}
	}

	private static void clearSingleTable(Connection connection, String tableName) {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("DELETE FROM " + tableName);
		} catch (SQLException ex) {
			throw new RuntimeException("Can't read table contents from table ".concat(tableName), ex);
		}
	}
}
