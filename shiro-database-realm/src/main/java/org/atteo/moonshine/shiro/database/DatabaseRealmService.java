/*
 * Copyright 2012 Atteo.
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
package org.atteo.moonshine.shiro.database;

import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.shiro.realm.Realm;
import org.atteo.moonshine.database.DatabaseMigration;
import org.atteo.moonshine.database.DatabaseService;
import org.atteo.moonshine.jpa.JpaService;
import org.atteo.moonshine.liquibase.LiquibaseFacade;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.shiro.RealmService;
import org.atteo.moonshine.springdata.RepositoryFactoryProvider;
import org.atteo.moonshine.springdata.RepositoryProvider;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

/**
 * Realm which keeps accounts in the database.
 *
 * <p>
 * Binds {@link Realm} and {@link AccountRepository}.
 * </p>
 */
@XmlRootElement(name = "database")
public class DatabaseRealmService extends RealmService {
	@ImportService
	@XmlIDREF
	private JpaService jpa;

	@ImportService
	private DatabaseService database;

	@Override
	public Module configure() {
		// we need to use the same database as JPA which we are using
		database = jpa.getDatabaseService();

		database.registerMigration(new DatabaseMigration() {
			@Override
			public void execute(DataSource dataSource) {
				new LiquibaseFacade(dataSource).migrate("liquibase/database-realm.xml");
			}
		});


		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(RepositoryFactorySupport.class).toProvider(RepositoryFactoryProvider.class)
						.in(Singleton.class);
				bind(AccountRepository.class).toProvider(new RepositoryProvider<>(AccountRepository.class))
						.in(Singleton.class);

				bind(Realm.class).to(DatabaseRealm.class);

				expose(AccountRepository.class);
				expose(Realm.class);
			}
		};
	}
}
