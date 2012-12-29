package org.atteo.evo.tests;

import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.services.TopLevelService;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

@XmlRootElement(name = "fixtures")
public class Fixtures extends TopLevelService {
	@Override
	public Module configure() {
		return new Module() {

			@Override
			public void configure(Binder binder) {
				FixtureInterceptor interceptor = new FixtureInterceptor();
				binder.requestInjection(interceptor);
				binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Fixture.class),
						interceptor);
			}
		};
	}
}
