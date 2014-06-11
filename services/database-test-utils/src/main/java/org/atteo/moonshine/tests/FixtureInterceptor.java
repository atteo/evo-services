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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.atteo.moonshine.liquibase.LiquibaseFacade;

import com.google.common.base.Strings;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class FixtureInterceptor implements MethodInterceptor {

	@Inject
	private Injector injector;

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		final Fixture annotation = invocation.getMethod().getAnnotation(Fixture.class);
		String[] fixtureNames = annotation.value();
		String databaseName = annotation.database();
		Class<? extends ChangelogParametersProvider> providerClass = annotation.parametersProvider();

		ChangelogParametersProvider provider = providerClass.newInstance();

		DataSource dataSource;
		try {
			if (Strings.isNullOrEmpty(databaseName)) {
				dataSource = injector.getInstance(DataSource.class);
			} else {
				dataSource = injector.getInstance(Key.get(DataSource.class, Names.named(databaseName)));
			}
		} catch (ConfigurationException e) {
			throw new RuntimeException("Cannot find database for annotation " + annotation, e);
		}

		for (int i = 0; i < fixtureNames.length; i++) {
			if (!fixtureNames[i].startsWith("/")) {
				fixtureNames[i] = "/" + invocation.getMethod().getDeclaringClass().getPackage().getName()
				    .replace('.', '/')
				    + "/" + fixtureNames[i];
			}
		}

		LiquibaseFacade liquibase = new LiquibaseFacade(dataSource);

		for (String fixtureName : fixtureNames) {
			liquibase.migrate(fixtureName, null, provider.getChangelogParameters());
		}

		Object o = null;
		try {
			o = invocation.proceed();
		} finally {
			for (int i = fixtureNames.length - 1; i >= 0; i--) {
				liquibase.rollbackLastUpdate(fixtureNames[i], null, provider.getChangelogParameters());
			}
		}
		return o;
	}
}
