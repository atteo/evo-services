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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atteo.evo.services.Service;
import org.atteo.evo.services.Services;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import static org.mockito.Mockito.mock;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.GuiceFilter;

/**
 * JUnit {@link MethodRule rule} which initializes {@link Services} environment.
 *
 * <p>
 * The {@link Services} engine will be initialized with the specified configuration
 * file. All {@link Service services} will be started and the test class itself
 * will be registered for members injection.
 * </p>
 * <p>
 * In addition any field marked with {@link Mock} annotation will be initialized
 * with the mocked object. If also marked with {@link Inject}
 * then the mocked object will be registered as an instance binding in Guice injector.
 * </p>
 * <p>
 * Any method marked with {@link Bindings} annotation will executed with {@link Binder}
 * as a sole parameter to allow you to register additional Guice bindings.
 * </p>
 */
public class ServicesRule implements TestRule {
	private String config;
	private Services services;
	private Object target;

	/**
	 * Initializes {@link Services} environment from "/test-config.xml" configuration file.
	 * <p>
	 * Usage:
	 * <pre>
	 * {@code
	 * class Test {
	 *     private ServicesRule services = new ServicesRule(this);
	 * }
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param target object of the executed test, usually just 'this'
	 */
	public ServicesRule(Object target) {
		this.target = target;
	}

	/**
	 * Initializes {@link Services} environment from given configuration file.
	 *
	 * @param target object of the executed test, usually just 'this'
	 * @param config
	 *            resource path to the configuration file
	 */
	public ServicesRule(Object target, String config) {
		this.target = target;
		this.config = config;
	}

	@Override
	public Statement apply(final Statement base, Description method) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				configure();
				try {
					base.evaluate();
				} finally {
					deconfigure();
				}
			}
		};
	}

	private void configure() {
		TestClass testClass = new TestClass(target.getClass());
		final List<FrameworkMethod> bindingsMethods = testClass.getAnnotatedMethods(Bindings.class);

		Module bindingsModule = new Module() {
			@Override
			public void configure(Binder binder) {
				for (int i = 0; i < bindingsMethods.size(); i++) {
					FrameworkMethod method = bindingsMethods.get(i);
					method.getMethod().setAccessible(true);
					try {
						method.invokeExplosively(target, binder);
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				}
			}
		};

		Class<?> klass = target.getClass();
		final Map<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();
		final Field fields[] = klass.getDeclaredFields();

		for (final Field field : fields) {
			if (field.isAnnotationPresent(Mock.class)) {
				final Object m = mock(field.getType());
				if (field.isAnnotationPresent(Inject.class)
						|| field.isAnnotationPresent(javax.inject.Inject.class)) {
					mocks.put(field.getType(), m);
				}
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

		Module mocksModule = new Module() {
			@SuppressWarnings("unchecked")
			@Override
			public void configure(final Binder binder) {
				// TODO: add support for binding annotated objects
				final Set<Class<?>> keys = mocks.keySet();
				final Iterator<Class<?>> i = keys.iterator();
				while (i.hasNext()) {
					final Class<?> klass = i.next();
					@SuppressWarnings("rawtypes")
					final TypeLiteral t = TypeLiteral.get(klass);
					final Object o = mocks.get(klass);
					binder.bind(t).toInstance(o);
				}
			}
		};

		InputStream stream;
		if (config != null) {
			stream = target.getClass().getResourceAsStream(config);
		} else {
			stream = target.getClass().getResourceAsStream("/test-config.xml");
		}

		if (stream == null && config != null) {
			throw new RuntimeException("Configuration resource not found: " + config);
		}

		// TODO: where to get those paths from?
		services = new Services(new File("target/test-home/"), new File("src/main/webapp/"), stream);
		services.addModule(bindingsModule);
		services.addModule(mocksModule);
		services.setThrowErrors(true);
		services.start();

		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		if (services.injector() != null) {
			services.injector().injectMembers(target);
		}
	}

	private void deconfigure() {
		if (services != null) {
			services.stop();
		}
		// Workaround for the WARNING: Multiple Servlet injectors detected.
		new GuiceFilter().destroy();
	}
}
