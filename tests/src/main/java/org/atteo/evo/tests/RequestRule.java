package org.atteo.evo.tests;

import java.util.HashMap;
import java.util.concurrent.Callable;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.Key;
import com.google.inject.servlet.ServletScopes;

/**
 * With this rule added the test acts as if it were executed as a part of a HTTP request.
 *
 * <p>
 * The test methods of the JUnit test will be executed
 * in {@link ServletScopes#REQUEST REQUEST} scope
 * using {@link ServletScopes#scopeRequest(java.util.concurrent.Callable, java.util.Map)}.
 * </p>
 */
public class RequestRule implements MethodRule {
	@Override
	public Statement apply(final Statement base, FrameworkMethod method, Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				ServletScopes.scopeRequest(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							base.evaluate();
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
						return null;
					}

				}, new HashMap<Key<?>, Object>()).call();
			}
		};
	}
}
