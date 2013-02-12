
package org.atteo.evo.tests;

import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.database.DatabaseService;
import org.atteo.evo.services.TopLevelService;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

@XmlRootElement(name = "database-cleaner")
public class DatabaseCleanerService extends TopLevelService {
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
					bind(DatabaseCleaner.class).toProvider(new DatabaseCleanerProvider()).in(Scopes.SINGLETON);
				} else {
					bind(Key.get(DatabaseCleaner.class, Names.named(id))).toProvider(
							new DatabaseCleanerProvider()).in(Scopes.SINGLETON);
				}
			}
		};
	}

	private class DatabaseCleanerProvider implements Provider<DatabaseCleaner> {
		@Inject
		private MembersInjector<DatabaseCleaner> injector;

		@Inject
		private Injector ginjector;

		@Override
		public DatabaseCleaner get() {
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
			DatabaseCleaner databaseCleaner = new DatabaseCleaner(dataSource);
			injector.injectMembers(databaseCleaner);
			return databaseCleaner;

		}
	}
}
