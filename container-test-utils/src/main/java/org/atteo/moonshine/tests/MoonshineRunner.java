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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.atteo.moonshine.Moonshine;
import org.atteo.moonshine.services.Services;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Runs the tests with {@link Services} framework initialized.
 *
 * <p>
 * Any method marked with {@link Bindings} annotation will executed with {@link Binder}
 * as a sole parameter to allow you to register additional Guice bindings.
 * </p>
 * <p>
 * The {@link Services} engine will be initialized with the specified configuration
 * file. All {@link Service services} will be started.
 * </p>
 * <p>
 * The test class will be instantiated using created Guice injector.
 * </p>
 */
public class MoonshineRunner extends BlockJUnit4ClassRunner {
	private MoonshineRule moonshineRule = null;

	public MoonshineRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected Object createTest() throws Exception {
		return moonshineRule.getGlobalInjector().getInstance(getTestClass().getJavaClass());
	}

	@Override
	protected List<TestRule> classRules() {
		if (getTestClass().getJavaClass().isAnnotationPresent(MoonshineConfiguration.class)) {
			final MoonshineConfiguration annotation = getTestClass().getJavaClass()
					.getAnnotation(MoonshineConfiguration.class);
			String[] configs = annotation.value();

			for (int i = 0; i < configs.length; i++) {
				if (!configs[i].startsWith("/")) {
					configs[i] = "/" + getTestClass().getJavaClass().getPackage() .getName().replace(".", "/") + "/"
							+ configs[i];
				}
			}
			List<MoonshineConfigurator> configurators = new ArrayList<>();

			Class<? extends MoonshineConfigurator> configuratorKlass = annotation.configurator();
			if (configuratorKlass != null && configuratorKlass != MoonshineConfigurator.class) {
				try {
					MoonshineConfigurator configurator = configuratorKlass.getConstructor().newInstance();
					configurators.add(configurator);
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
			MoonshineConfigurator configurator = new MoonshineConfigurator() {
				@Override
				public void configureMoonshine(Moonshine.Builder builder) {
					if (annotation.skipDefault()) {
						builder.skipDefaultConfigurationFiles();
					}
					if (!annotation.fromString().isEmpty()) {
						builder.addConfigurationFromString(annotation.fromString());
					}

					builder.arguments(annotation.arguments());
				}
			};
			configurators.add(configurator);

			moonshineRule = new MoonshineRule(configurators, configs);
		} else {
			moonshineRule = new MoonshineRule();
		}

		List<TestRule> rules = super.classRules();
		rules.add(moonshineRule);
		return rules;
	}

	@Override
	protected List<TestRule> getTestRules(Object target) {
		List<TestRule> rules = super.getTestRules(target);
		rules.add(new RequestRule());
		return rules;
	}

	@Override
	protected List<MethodRule> rules(Object target) {
		List<MethodRule> rules = super.rules(target);
		rules.add(moonshineRule.injectMembers(target));
		rules.add(new MockitoRule());
		return rules;
	}
}
