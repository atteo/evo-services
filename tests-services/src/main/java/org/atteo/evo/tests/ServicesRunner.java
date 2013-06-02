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

import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class ServicesRunner extends BlockJUnit4ClassRunner {
	private ServicesRule servicesRule;

	public ServicesRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected Object createTest() throws Exception {
		return servicesRule.getServices().injector().getInstance(getTestClass().getJavaClass());
	}

	@Override
	protected List<TestRule> classRules() {
		if (getTestClass().getJavaClass().isAnnotationPresent( ServicesConfiguration.class)) {
			String[] configs = getTestClass().getJavaClass().getAnnotation(ServicesConfiguration.class).value();

			for (int i = 0; i < configs.length; i++) {
				if (!configs[i].startsWith("/")) {
					configs[i] = "/" + getTestClass().getJavaClass().getPackage() .getName().replace(".", "/") + "/"
							+ configs[i];
				}
			}

			servicesRule = new ServicesRule(configs);
		} else {
			servicesRule = new ServicesRule();
		}

		List<TestRule> rules = super.classRules();
		rules.add(servicesRule);
		return rules;
	}

	@Override
	protected List<TestRule> getTestRules(Object target) {
		List<TestRule> rules = super.getTestRules(target);
		rules.add(new InjectionRule(target));
		rules.add(new RequestRule());
		return rules;
	}
}
