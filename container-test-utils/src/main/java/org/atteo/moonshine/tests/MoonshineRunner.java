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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.atteo.moonshine.Moonshine;
import org.atteo.moonshine.tests.MoonshineConfiguration.Config;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * Runs the tests inside {@link Moonshine} container.
 *
 * <p>
 * You can configure the container by annotating the class with
 * {@link MoonshineConfiguration}.
 * </p>
 * <p>
 * The test class will be instantiated using global Guice injector of the
 * Moonshine container.
 * </p>
 */
public class MoonshineRunner extends BlockJUnit4ClassRunner {

	private MoonshineRule moonshineRule = null;
	private boolean requestPerClass = false;
	private final List<Config> iterationConfigs;
	private final List<String> iterationIds;
	private final Class<?> klass;

	public MoonshineRunner(Class<?> klass) throws InitializationError {
		super(klass);
		this.klass = klass;
		iterationConfigs = Collections.emptyList();
		iterationIds = Collections.emptyList();
	}

	/**
	 * Used by {@link MoonshineMultiRunner}.
	 */
	MoonshineRunner(Class<?> klass, List<Config> iterationConfigs) throws InitializationError {
		super(klass);
		this.klass = klass;
		this.iterationConfigs = iterationConfigs;
		iterationIds = getIterationIds(iterationConfigs);
	}

	@Override
	protected Object createTest() throws Exception {
		return moonshineRule.getGlobalInjector().getInstance(getTestClass().getJavaClass());
	}

	@Override
	protected List<TestRule> classRules() {
		@SuppressWarnings("unchecked")
		Set<Class<?>> ancestorSet = (Set<Class<?>>) TypeToken.of(getTestClass().getJavaClass()).getTypes().rawTypes();
		List<Class<?>> ancestors = Lists.reverse(new ArrayList<>(ancestorSet));

		final List<String> configPaths = new ArrayList<>();
		List<MoonshineConfigurator> configurators = new ArrayList<>();
		AtomicBoolean loadTestConfigXml = new AtomicBoolean(true);

		for (Class<?> ancestor : ancestors) {
			analyseAncestor(ancestor, configPaths, configurators, loadTestConfigXml);
		}

		analyseIterationConfigs(configPaths, configurators);

		moonshineRule = new MoonshineRule(configurators, configPaths.toArray(new String[configPaths.size()]));
		moonshineRule.setLoadTestConfigXml(loadTestConfigXml.get());

		List<TestRule> rules = super.classRules();
		if (requestPerClass) {
			rules.add(new RequestRule());
		}

		rules.add(moonshineRule);
		return rules;
	}

	@Override
	protected String getName() {
		String name = super.getName();
		if (!iterationConfigs.isEmpty()) {
			return name + " with config [" + Joiner.on(",").join(iterationIds) + "]";
		}
		return name;
	}

	@Override
	public Description getDescription() {
		Description description = Description.createTestDescription(klass, getName(), getRunnerAnnotations());
		for (FrameworkMethod child : getChildren()) {
			description.addChild(describeChild(child));
		}
		return description;
	}

	private void analyseIterationConfigs(final List<String> configs, List<MoonshineConfigurator> configurators) {
		if (iterationConfigs.isEmpty()) {
			return;
		}
		for (Config config : iterationConfigs) {
			if (config.value().length != 0) {
				configs.addAll(Arrays.asList(config.value()));
			}
		}

		configurators.add((MoonshineConfigurator) (Moonshine.Builder builder) -> {
			for (Config config : iterationConfigs) {
				if (!config.fromString().isEmpty()) {
					builder.addConfigurationFromString(config.fromString());
				}
			}
			builder.addModule(new AbstractModule() {
				@Override
				protected void configure() {
					bind(new TypeLiteral<List<String>>() {}).annotatedWith(EnabledConfigs.class)
							.toInstance(iterationIds);
				}
			});
			builder.applicationName(klass.getSimpleName() + "[" + Joiner.on(",").join(iterationIds) + "]");
		});
	}

	private static List<String> getIterationIds(List<Config> iterationConfigs) {
		final List<String> iterationIds = new ArrayList<>();
		for (Config config : iterationConfigs) {
			iterationIds.add(config.id());
		}
		return iterationIds;
	}

	private static String getPathToResource(Class<?> klass, String annotationValue) {
		if (annotationValue.startsWith("/")) {
			return annotationValue;
		} else {
			return "/" + klass.getPackage().getName().replace(".", "/") + "/" + annotationValue;
		}
	}

	private void analyseAncestor(Class<?> ancestor, final List<String> configs,
			List<MoonshineConfigurator> configurators, final AtomicBoolean loadTestConfigXml) {
		final MoonshineConfiguration annotation = ancestor.getAnnotation(MoonshineConfiguration.class);
		if (annotation == null) {
			return;
		}
		loadTestConfigXml.set(false);
		for (String config : annotation.value()) {
			configs.add(getPathToResource(ancestor, config));
		}
		requestPerClass = annotation.oneRequestPerClass();
		if ((annotation.forEach().length != 0 || annotation.forCartesianProductOf().length != 0)
				&& iterationConfigs.isEmpty()) {
			throw new RuntimeException("Error on class " + ancestor.getName()
					+ ": @" + MoonshineConfiguration.class.getSimpleName()
					+ " forEach and forCartesianProductOf can be used only with "
					+ MoonshineMultiRunner.class.getSimpleName());
		}
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
		if (annotation.autoConfiguration() || annotation.skipDefault() || !annotation.fromString().isEmpty()
				|| annotation.arguments().length != 0) {
			MoonshineConfigurator configurator = (Moonshine.Builder builder) -> {
				if (annotation.autoConfiguration()) {
					builder.autoConfiguration();
				}
				if (annotation.skipDefault()) {
					builder.skipDefaultConfigurationFiles();
				}
				if (!annotation.fromString().isEmpty()) {
					builder.addConfigurationFromString(annotation.fromString());
				}
				
				builder.arguments(annotation.arguments());
			};
			configurators.add(configurator);
		}
	}

	@Override
	protected List<TestRule> getTestRules(Object target) {
		List<TestRule> rules = super.getTestRules(target);
		if (!requestPerClass) {
			rules.add(new RequestRule());
		}
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
