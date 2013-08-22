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
import org.atteo.moonshine.database.DatabaseService;
import org.atteo.moonshine.jpa.JpaService;
import org.atteo.moonshine.liquibase.LiquibaseFacade;
import org.atteo.moonshine.services.ImportBindings;
import org.atteo.moonshine.shiro.RealmService;
import org.atteo.moonshine.springdata.DatabaseInitializer;
import org.atteo.moonshine.springdata.RepositoryFactoryProvider;
import org.atteo.moonshine.springdata.RepositoryProvider;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;

/**
 * Realm which keeps accounts in the database.
 *
 * <p>
 * Binds {@link Realm} and {@link AccountRepository}.
 * </p>
 */
@XmlRootElement(name = "database")
public class DatabaseRealmService extends RealmService {
	@ImportBindings
	@XmlIDREF
	private DatabaseService database;

	@ImportBindings
	@XmlIDREF
	private JpaService jpa;

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Provides
			@Singleton
			public DatabaseInitializer migrateDatabase(DataSource dataSource) {
				new LiquibaseFacade(dataSource).migrate("liquibase/database-realm.xml");

				return new DatabaseInitializer() { };
			}

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
