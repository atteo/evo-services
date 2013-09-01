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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <atomikos/>"
		+ "    <h2/>"
		+ "    <liquibase/>"
		+ "</config>")
public class LiquibaseServiceTest extends MoonshineTest {
	@Inject
	private DataSource dataSource;

	@Inject
	private LiquibaseFacade migrations;

	private boolean userExists() throws SQLException {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(
						"select * from users where name = 'Joey Tribbiani';");
				ResultSet result = preparedStatement.executeQuery()) {
			return result.next();
		}
	}

	@Test
	public void testMigrations() throws SQLException {
		migrations.migrate("/test-migration1.xml");

		assertFalse(userExists());
		migrations.migrate("/test-migration2.xml");
		assertTrue((userExists()));
		migrations.rollbackLastUpdate("/test-migration2.xml");
		assertFalse(userExists());
	}
}
