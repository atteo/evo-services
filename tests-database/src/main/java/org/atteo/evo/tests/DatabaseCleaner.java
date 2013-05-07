package org.atteo.evo.tests;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class DatabaseCleaner {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseCleaner.class);
	private final DataSource dataSource;

	@Inject
	public DatabaseCleaner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void clean() {
		logger.debug("Clearing database");
		try (Connection connection = dataSource.getConnection()) {

			List<String> tables = analyseDatabase(connection);

			clearTables(connection, tables);
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

	private void clearTables(Connection connection, List<String> tables) {
		for (String table : tables) {
			clearSingleTable(connection, table);
		}
	}

	private void dropTable(Connection connection, String tableName) {
		try (PreparedStatement statement = connection.prepareStatement("DROP TABLE ?")) {
			statement.setString(1, tableName);
			statement.executeUpdate();
		} catch (SQLException ex) {
			throw new RuntimeException("Can't read table contents from table ".concat(tableName), ex);
		}
	}

	private void clearSingleTable(Connection connection, String tableName) {
		try (PreparedStatement statement = connection.prepareStatement("DELETE FROM ?")) {
			statement.setString(1, tableName);
			statement.executeUpdate();
		} catch (SQLException ex) {
			throw new RuntimeException("Can't read table contents from table ".concat(tableName), ex);
		}
	}

}
