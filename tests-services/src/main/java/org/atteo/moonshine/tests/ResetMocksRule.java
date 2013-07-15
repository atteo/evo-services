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

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;

/**
 * Resets the mocks registered using {@link MockAndBind}.
 * <p>
 * Usage:
 * {@code
 * class Test {
 *     @ClassRule
 *     public static final MoonshineRule moonshine = new Moonshine();
 *
 *     @Rule
 *     public ResetMocksRule resetMocks = new ResetMocksRule(moonshine);
 *
 * }
 */
public class ResetMocksRule implements MethodRule {
	private MoonshineRule servicesRule;

	public ResetMocksRule(MoonshineRule servicesRule) {
		this.servicesRule = servicesRule;
	}

	@Override
	public Statement apply(final Statement base, FrameworkMethod method, final Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				resetMocks();
				base.evaluate();
				Mockito.validateMockitoUsage();
			}
		};
	}

	private void resetMocks() {
		for (Object mock : servicesRule.getMocks().values()) {
			Mockito.reset(mock);
		}
	}

}
