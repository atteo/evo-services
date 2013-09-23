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
package org.atteo.moonshine.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.jta.Transaction;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.TopLevelService;

import com.google.inject.Module;

@XmlRootElement(name = "migration-service")
public class MigrationService extends TopLevelService {
	@ImportService
	private DatabaseService database;

	@Override
	public Module configure() {
		database.registerMigration(new DatabaseMigration() {
			@Override
			public void execute(final DataSource dataSource) {
				Transaction.require(new Transaction.Runnable() {
					@Override
					public void run() {
						try (final Connection connection = dataSource.getConnection();
								final Statement statement = connection.createStatement()) {
							statement.execute("create table users (name varchar(256))");
							statement.execute("insert into users(name) values ('John')");
						} catch (SQLException ex) {
							throw new RuntimeException(ex);
						}
					}
				});
			}
		});
		return null;
	}

}
