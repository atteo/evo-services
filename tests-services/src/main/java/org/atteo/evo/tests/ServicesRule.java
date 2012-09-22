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
package org.atteo.evo.tests;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.services.Service;
import org.atteo.evo.services.Services;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.GuiceFilter;

/**
 * JUnit {@link TestRule rule} which initializes {@link Services} environment.
 *
 * <pre>
 * {@code
 * class Test {
 *     @ClassRule
 *     private static ServicesRule services = new ServicesRule();
 *     @Rule
 *     private InjectionRule injections = new InjectionRule(this, services);
 * }
 * }
 * </pre>
 * <p>
 * The {@link Services} engine will be initialized with the specified configuration
 * file. All {@link Service services} will be started.
 * </p>
 * <p>
 * Any method marked with {@link Bindings} annotation will executed with {@link Binder}
 * as a sole parameter to allow you to register additional Guice bindings.
 * </p>
 */
public class ServicesRule implements TestRule {
	public final static String[] DEFAULT_CONFIG = { "/test-config.xml" };
	private String[] configs;
	private Services services;

	/**
	 * Initializes {@link Services} environment from "/test-config.xml" configuration file.
	 * <p>
	 * Usage:
	 * <pre>
	 * {@code
	 * class Test {
	 *     private static ServicesRule services = new ServicesRule();
	 * }
	 * }
	 * </pre>
	 * </p>
	 */
	public ServicesRule() {
		this.configs = DEFAULT_CONFIG;
	}

	/**
	 * Initializes {@link Services} environment from given configuration files.
	 *
	 * @param configs resource path to the configuration files
	 */
	public ServicesRule(String[] configs) {
		this.configs = configs;
	}

	/**
	 * Initializes {@link Services} environment from given configuration file.
	 *
	 * @param config resource path to the configuration file
	 */
	public ServicesRule(String config) {
		this.configs = new String[] { config };
	}

	@Override
	public Statement apply(final Statement base, final Description method) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				configure(method.getTestClass());
				try {
					base.evaluate();
				} finally {
					deconfigure();
				}
			}
		};
	}

	private void configure(final Class<?> klass) {
		Module testClassModule = new Module() {
			@Override
			public void configure(Binder binder) {
				binder.bind(klass);
			}
		};

		Module bindingsModule = new Module() {
			@Override
			public void configure(Binder binder) {
				for (Method method : klass.getMethods()) {
					if (!method.isAnnotationPresent(Bindings.class)) {
						continue;
					}
					if (!Modifier.isStatic(method.getModifiers())) {
						throw new RuntimeException("Method annotated with @Bindings annotation must be static");
					}
					if (method.getParameterTypes().length != 1 || method.getParameterTypes()[0] != Binder.class) {
						throw new RuntimeException("Method annotated with @Bindings must have exactly"
								+ " one argument of type com.google.inject.Binder");
					}

					method.setAccessible(true);
					try {
						method.invoke(null, binder);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};

		final Map<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();
		final Field fields[] = klass.getDeclaredFields();

		for (final Field field : fields) {
			if (field.isAnnotationPresent(Mock.class)
					&& (field.isAnnotationPresent(javax.inject.Inject.class)
					|| field.isAnnotationPresent(Inject.class))) {
				Object object = mock(field.getType());
				mocks.put(field.getType(), object);
			}
		}

		Module mocksModule = new Module() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure(final Binder binder) {
				// TODO: add support for binding annotated objects
				for (Class<?> klass : mocks.keySet()) {
					@SuppressWarnings("rawtypes")
					final TypeLiteral t = TypeLiteral.get(klass);
					final Object object = mocks.get(klass);
					binder.bind(t).toInstance(object);
				}

				binder.requestStaticInjection(klass);
			}
		};

		try {
			services = new Services();
			services.setHomeDirectory(new File("target/test-home"));
			for (String config : configs) {
				services.combineConfigurationFromResource(config, configs != DEFAULT_CONFIG);
			}
		services.addModule(testClassModule);
			services.addModule(bindingsModule);
			services.addModule(mocksModule);
			services.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (IncorrectConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private void deconfigure() {
		if (services != null) {
			services.stop();
		}
		// Workaround for the WARNING: Multiple Servlet injectors detected.
		new GuiceFilter().destroy();
	}

	public Services getServices() {
		return services;
	}
}
