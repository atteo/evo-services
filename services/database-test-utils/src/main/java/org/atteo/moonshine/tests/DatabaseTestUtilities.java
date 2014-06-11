/*
 * Copyright 2011 Atteo.
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

import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.database.DatabaseService;
import org.atteo.moonshine.services.ImportService;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;

@XmlRootElement(name = "database-tests")
public class DatabaseTestUtilities extends TopLevelService {
	@ImportService
	@XmlIDREF
	@XmlElement(name = "database")
	private DatabaseService database;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(DatabaseCleaner.class).toProvider(new Provider<DatabaseCleaner>() {
					@Inject
					private DataSource dataSource;

					@Override
					public DatabaseCleaner get() {
						return new DatabaseCleaner(dataSource, database);
					}
				}).in(Scopes.SINGLETON);

				FixtureInterceptor interceptor = new FixtureInterceptor();
				requestInjection(interceptor);
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Fixture.class), interceptor);
			}
		};
	}
}
