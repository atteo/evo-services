/*
 * Copyright 2013 Atteo.
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.atteo.moonshine.liquibase.LiquibaseFacade;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class FixtureInterceptorTest extends MoonshineTest {

	@Inject
	private DataSource dataSource;

	@Fixture(value = "/fixture.xml", parametersProvider = UsernameProvider.class)
	protected void annotatedWithOneFixture() throws SQLException {
		assertTrue(userExists(dataSource));
	}

	@Test
	public void shouldExecuteOneFixture() throws SQLException {
		LiquibaseFacade liquibase = new LiquibaseFacade(dataSource);
		liquibase.migrate("/initialization.xml");

		assertFalse(userExists(dataSource));
		annotatedWithOneFixture();
		assertFalse(userExists(dataSource));
	}

	public static boolean userExists(DataSource dataSource) throws SQLException {
		try (final Connection connection = dataSource.getConnection();
				final PreparedStatement preparedStatement = connection.prepareStatement(
						"select * from users where name = 'john';");
				final ResultSet result = preparedStatement.executeQuery()) {
			return result.next();
		}
	}
}
