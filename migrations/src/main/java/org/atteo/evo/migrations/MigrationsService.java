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
package org.atteo.evo.migrations;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.database.DatabaseService;
import org.atteo.evo.services.TopLevelService;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

@XmlRootElement(name = "migrations")
public class MigrationsService extends TopLevelService {
	@XmlElement
	@XmlIDREF
	private DatabaseService database;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				String id = getId();
				if (id == null) {
					bind(Migrations.class).toProvider(new MigrationsProvider()).in(Scopes.SINGLETON);
				} else {
					bind(Key.get(Migrations.class, Names.named(id))).toProvider(
							new MigrationsProvider()).in(Scopes.SINGLETON);
				}
			}
		};
	}

	private class MigrationsProvider implements Provider<Migrations> {
		@Inject
		private MembersInjector<Migrations> injector;

		@Inject
		private Injector ginjector;

		@Override
		public Migrations get() {
			String id = null;
			if (database != null) {
				id = database.getId();
			}

			DataSource dataSource;
			if (id != null) {
				dataSource = ginjector.getInstance(Key.get(DataSource.class, Names.named(id)));
			} else {
				dataSource = ginjector.getInstance(DataSource.class);
			}
			Migrations migrations = new Migrations(dataSource);
			injector.injectMembers(migrations);
			return migrations;

		}
	}

}
