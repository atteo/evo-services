/*
 * Copyright 2013 Atteo.
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

import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.Injector;

/**
 * Executes {@link Injector#injectMembers} on test class.
 *
 * <p>
 * It is better for your test class to extend {@link ServicesTest} or have it annotated with
 * &#064;{@link RunWith}({@link ServicesRunner ServicesRunner.class}). With those solutions
 * the test class is created using Guice injector.
 * </p>
 * <p>
 * The advantage of this rule is that you an use it with any custom runner. The disadvantage
 * is that Guice AOP won't work on test class.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * class Test {
 *     &#064;ClassRule
 *     private static ServicesRule services = new ServicesRule();
 *     &#064;Rule
 *     private InjectionRule injections = new InjectionRule(this, services);
 * }
 * </pre>
 * </p>
 */
public class InjectionRule implements MethodRule {
	private ServicesRule servicesRule;

	public InjectionRule(ServicesRule servicesRule) {
		this.servicesRule = servicesRule;
	}

	@Override
	public Statement apply(final Statement base, FrameworkMethod method, final Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				if (servicesRule.getServices().injector() != null) {
					servicesRule.getServices().injector().injectMembers(target);
				}
				base.evaluate();
			}
		};

	}

}
