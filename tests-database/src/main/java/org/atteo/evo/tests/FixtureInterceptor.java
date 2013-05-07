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
package org.atteo.evo.tests;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.atteo.evo.migrations.Migrations;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class FixtureInterceptor implements MethodInterceptor {
	@Inject
	Injector injector;

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		String fixtureName = invocation.getMethod().getAnnotation(Fixture.class).value();
		String databaseName = invocation.getMethod().getAnnotation(Fixture.class).database();

		Migrations migrations;

		if (Strings.isNullOrEmpty(databaseName)) {
			migrations = injector.getInstance(Migrations.class);
		} else {
			migrations = injector.getInstance(Key.get(Migrations.class, Names.named(databaseName)));
		}

		if (fixtureName.startsWith("/")) {
			fixtureName = fixtureName.substring(1);
		} else {
			fixtureName = invocation.getMethod().getDeclaringClass().getPackage().getName()
					.replace('.', '/')
					+ "/" + fixtureName;
		}

		migrations.migrate(fixtureName);
		Object o = null;
		try {
			o = invocation.proceed();
		} finally {
			migrations.rollbackLastUpdate(fixtureName);
		}
		return o;
	}
}
