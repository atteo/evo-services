/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.moonshine.tests;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atteo.moonshine.Moonshine;
import org.atteo.moonshine.MoonshineException;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.GuiceFilter;

/**
 * JUnit {@link TestRule rule} which initializes {@link Moonshine} container.
 *
 * <p>
 * It is better for your test class to extend {@link MoonshineTest} or have it
 * annotated with
 * &#064;{@link RunWith}({@link MoonshineRunner MoonshineRunner.class}). With
 * those solutions the test class is created using Guice injector.
 * </p>
 *
 * <p>
 * Usage:
 * <pre>
 * class Test {
 *     &#064;ClassRule
 *     public static final MoonshineRule moonshine = new MoonshineRule();
 *     &#064;Rule
 *     public MethodRule injections = moonshine.injectMembers(this);
 * }
 * </pre>
 * </p>
 */
public class MoonshineRule implements TestRule {
	public final static String TEST_CONFIG = "/test-config.xml";
	private final String[] configs;
	private Moonshine moonshine;
	private final Map<Class<?>, Object> mocks = new HashMap<>();
	private List<MoonshineConfigurator> configurators = Collections.emptyList();
	private boolean loadTestConfigXml;

	Map<Class<?>, Object> getMocks() {
		return mocks;
	}

	/**
	 * Initializes {@link Moonshine} environment.
	 *
	 * <p>
	 * Usage:
	 * <pre>
	 * class Test {
	 *     &#064;ClassRule
	 *     public static final MoonshineRule moonshine = new MoonshineRule();
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param configs resource path to the configuration files, by default
	 * "/test-config.xml"
	 */
	public MoonshineRule(String... configs) {
		this.configs = configs;
		loadTestConfigXml = configs.length == 0;
	}

	/**
	 * Initializes {@link Moonshine} environment.
	 *
	 * @param configurator {@link MoonshineConfigurator configurator} for Moonshine
	 * @param configs resource path to the configuration files
	 */
	public MoonshineRule(MoonshineConfigurator configurator, String... configs) {
		this.configurators = Lists.newArrayList(configurator);
		this.configs = configs;
		loadTestConfigXml = configs.length == 0;
	}

	/**
	 * Initializes {@link Moonshine} environment.
	 *
	 * @param configurators list of {@link MoonshineConfigurator configurators} for Moonshine
	 * @param configs resource path to the configuration files
	 */
	public MoonshineRule(List<MoonshineConfigurator> configurators, String... configs) {
		this.configurators = configurators;
		this.configs = configs;
		loadTestConfigXml = configs.length == 0;
	}

	public void setLoadTestConfigXml(boolean loadTestConfigXml) {
		this.loadTestConfigXml = loadTestConfigXml;
	}

	@Override
	public Statement apply(final Statement base, final Description method) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try (Moonshine moonshine = buildMoonshine(method.getTestClass())) {
					MoonshineRule.this.moonshine = moonshine;
					if (moonshine != null) {
						moonshine.start();
					}

					base.evaluate();
				}
				// Workaround for the WARNING: Multiple Servlet injectors detected.
				new GuiceFilter().destroy();
				MoonshineRule.this.moonshine = null;
			}
		};
	}

	private Moonshine buildMoonshine(final Class<?> testClass) throws MoonshineException {
		try {
			Moonshine.Builder builder = Moonshine.Factory.builder();

			Module testClassModule = (Binder binder) -> {
				binder.bind(testClass);
			};

			final Field fields[] = testClass.getDeclaredFields();

			for (final Field field : fields) {
				if (field.isAnnotationPresent(MockAndBind.class)) {
					Object object = mock(field.getType());
					mocks.put(field.getType(), object);
				}
			}

			Module mocksModule = (final Binder binder) -> {
				// TODO: add support for binding annotated objects
				for (Class<?> klass : mocks.keySet()) {
					@SuppressWarnings("rawtypes")
					final TypeLiteral t = TypeLiteral.get(klass);
					final Object object = mocks.get(klass);
					binder.bind(t).toInstance(object);
				}
				
				binder.requestStaticInjection(testClass);
			};

			builder.applicationName(testClass.getSimpleName());

			String threadName = Thread.currentThread().getName();

			builder.homeDirectory("target/test-home-" + threadName);
			builder.addDataDir("src/main");

			for (String config : configs) {
				builder.addConfigurationFromResource(config);
			}

			if (loadTestConfigXml) {
				builder.addOptionalConfigurationFromResource(TEST_CONFIG);
			}

			builder.addModule(testClassModule);
			builder.addModule(mocksModule);

			for (MoonshineConfigurator configurator : configurators) {
				configurator.configureMoonshine(builder);
			}
			return builder.build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Returns global {@link Injector}.
	 */
	public Injector getGlobalInjector() {
		return moonshine.getGlobalInjector();
	}

	/**
	 * Returns the rule which injects members of given object on each test run.
	 * <p>
	 * Usage:
	 * <pre>
	 * class Test {
	 *     &#064;ClassRule
	 *     public static final MoonshineRule moonshine = new MoonshineRule();
	 *     &#064;Rule
	 *     public MethodRule injections = moonshine.injectMembers(this);
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param object object to inject members into
	 * @return the method rule to use with JUnit
	 */
	public MethodRule injectMembers(Object object) {
		return (final Statement base, FrameworkMethod method, final Object target) -> new Statement() {
			@Override
			public void evaluate() throws Throwable {
				if (getGlobalInjector() != null) {
					getGlobalInjector().injectMembers(target);
				}
				base.evaluate();
			}
		};
	}
}
