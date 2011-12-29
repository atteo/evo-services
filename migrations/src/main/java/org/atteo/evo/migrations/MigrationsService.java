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

	private Migrations migrations;
	
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
			if (database != null)
				id = database.getId();

			DataSource dataSource;
			if (id != null) {
				dataSource = ginjector.getInstance(Key.get(DataSource.class, Names.named(id)));
			} else {
				dataSource = ginjector.getInstance(DataSource.class);
			}
			migrations = new Migrations(dataSource);
			injector.injectMembers(migrations);
			return migrations;

		}
	}

}
