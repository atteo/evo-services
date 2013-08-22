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
package org.atteo.moonshine.tests;

import java.util.concurrent.Callable;

import org.apache.shiro.subject.Subject;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * With this rule added each test is executed within the scope of new Subject.
 */
public class ShiroRule implements TestRule {
	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				Subject.Builder builder = new Subject.Builder();
				Subject subject = builder.buildSubject();
				subject.execute(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							base.evaluate();
						} catch (Exception | Error e) {
							throw e;
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
						return null;
					}
				});
			}
		};
	}
}
