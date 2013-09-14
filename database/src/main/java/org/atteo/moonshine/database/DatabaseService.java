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

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.atteo.moonshine.services.TopLevelService;

/**
 * Database service.
 * <p>
 * Database services should bind {@link DataSource}.
 * </p>
 */
public abstract class DatabaseService extends TopLevelService {
	protected List<DatabaseMigration> migrations = new ArrayList<>();

	/**
	 * Register database migration.
	 */
	public void registerMigration(DatabaseMigration migration) {
		migrations.add(migration);
	}

	/**
	 * Execute registered database migrations.
	 */
	protected void executeMigrations(DataSource dataSource) {
		for (DatabaseMigration migration : migrations) {
			migration.execute(dataSource);
		}
		migrations = null;
	}
}
