
package org.atteo.moonshine.tests;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.database.DatabaseService;
import org.atteo.moonshine.services.ImportBindings;
import org.atteo.moonshine.services.TopLevelService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;

@XmlRootElement(name = "database-tests")
public class DatabaseTestUtilities extends TopLevelService {
	@XmlElement
	@XmlIDREF
	@ImportBindings
	private DatabaseService database;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(DatabaseCleaner.class).in(Scopes.SINGLETON);

				FixtureInterceptor interceptor = new FixtureInterceptor();
				requestInjection(interceptor);
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Fixture.class), interceptor);
			}
		};
	}
}
