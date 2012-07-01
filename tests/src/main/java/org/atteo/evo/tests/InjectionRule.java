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
package org.atteo.evo.tests;

import java.lang.reflect.Field;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import static org.mockito.Mockito.mock;

import com.google.inject.Inject;

/**
 * Injects non-static fields of test class using {@link Services} framework.
 * <p>
 * Usage:
 * <pre>
 * {@code
 * class Test {
 *     private static ServicesRule services = new ServicesRule();
 *     private InjectionRule injections = new InjectionRule(this, services);
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * In addition any field marked with {@link Mock} annotation and not marked with {@link Inject}
 * will be initialized with the mocked objects.
 * </p>
 */
public class InjectionRule implements TestRule {
	private Object target;
	private ServicesRule servicesRule;
	private boolean injectMembers = true;

	/**
	 * 
	 * @param target object of the executed test, usually just 'this'
	 */
	public InjectionRule(Object target, ServicesRule servicesRule) {
		this.target = target;
		this.servicesRule = servicesRule;
	}

	/**
	 * Internal. Called by {@link ServicesRunner}.
	 * @param target object of th executed test
	 */
	InjectionRule(Object target) {
		this.target = target;
		injectMembers = false;
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				configure();
				base.evaluate();
			}
		};
	}

	private void configure() {
		for (final Field field : target.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Mock.class)) {
				if (!field.isAnnotationPresent(Inject.class)
						|| field.isAnnotationPresent(javax.inject.Inject.class)) {
					final Object m = mock(field.getType());
					field.setAccessible(true);
					try {
						field.set(target, m);
					} catch (final IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (final IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		if (injectMembers) {
			if (servicesRule.getServices().injector() != null) {
				servicesRule.getServices().injector().injectMembers(target);
			}
		}
	}
}

