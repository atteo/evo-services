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
package org.atteo.moonshine.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.jta.Transaction;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Test;


@MoonshineConfiguration(autoConfiguration = true, fromString = ""
		+ "<config>"
		+ "    <btm>"
		+ "        <transactionTimeout>5</transactionTimeout>"
		+ "    </btm>"
		+ "</config>")
public abstract class DatabaseTest extends MoonshineTest {
	@Inject
	private DataSource dataSource;

	@Test
	public void shouldInjectDataSource() {
		assertThat(dataSource).isNotNull();
	}

	@Test
	public void shouldSelectJohn() throws SQLException {
		Transaction.require(new Transaction.ThrowingRunnable<SQLException>() {
			@Override
			public void run() throws SQLException {
				try (Connection connection = dataSource.getConnection()) {
					try (PreparedStatement statement = connection.prepareStatement("select * from users")) {
						try (ResultSet result = statement.executeQuery()) {
							assertThat(result.next()).isEqualTo(true);
							assertThat(result.getString(1)).isEqualTo("John");
							assertThat(result.next()).isEqualTo(false);
						}
					}
				}
			}
		});
	}
}
