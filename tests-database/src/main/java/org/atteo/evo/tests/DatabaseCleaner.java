package org.atteo.evo.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseCleaner {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseCleaner.class);

	private final DataSource dataSource;

	public DatabaseCleaner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void clear() {
		logger.debug("Clearing database");
		Connection connection = null;
		try {
			connection = establishConnection();

			ArrayList<String> tables = analyseDatabase(connection);

			clearTables(connection, tables);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
				}
			}
		}

	}

	private Connection establishConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException ex) {
			throw new RuntimeException(
					"An exception occured while trying to"
							+ "connect to the database.", ex);
		}

	}

	private ArrayList<String> analyseDatabase(Connection connection) {
		ResultSet result = null;
		try {
			ArrayList<String> tables = new ArrayList<String>();

			DatabaseMetaData metaData = connection.getMetaData();

			result = metaData.getTables(null, null, "%",
					new String[] { "TABLE" });

			while (result.next()) {
				tables.add(result.getString("TABLE_NAME"));
			}

			return tables;
		} catch (SQLException ex) {
			throw new RuntimeException(
					"An exception occured while trying to"
							+ "analyse the database.", ex);
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException ex) {
					logger.error(null, ex);
				}
			}

		}
	}

	private void clearTables(Connection connection, ArrayList<String> tables) {
		for (String table : tables) {
			clearSingleTable(connection, table);
		}
	}

	private void clearSingleTable(Connection connection, String tableName) {
		try {
			connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE).execute(
					"DELETE FROM ".concat(tableName));

		} catch (SQLException ex) {
			throw new RuntimeException( "Can't read table contents from table ".concat(tableName), ex);
		}
	}

}
