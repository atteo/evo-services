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
package org.atteo.moonshine.tests;

import java.util.HashMap;
import java.util.concurrent.Callable;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Key;
import com.google.inject.servlet.ServletScopes;

/**
 * With this rule added the test acts as if it were executed as a part of a HTTP request.
 *
 * <p>
 * The test methods of the JUnit test will be executed
 * in {@link ServletScopes#REQUEST REQUEST} scope
 * using {@link ServletScopes#scopeRequest(java.util.concurrent.Callable, java.util.Map)}.
 * </p>
 */
public class RequestRule implements TestRule {
	@Override
	public Statement apply(final Statement base, Description method) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				ServletScopes.scopeRequest(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							base.evaluate();
						} catch (RuntimeException | Error e) {
							// don't wrap RuntimeExceptions
							// don't wrap Errors either, JUnit throws this
							throw e;
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
						return null;
					}

				}, new HashMap<Key<?>, Object>()).call();
			}
		};
	}
}
