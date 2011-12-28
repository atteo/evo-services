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
package org.atteo.evo.janino;

import java.lang.reflect.InvocationTargetException;

import org.atteo.evo.filtering.PropertyResolver;
import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.util.LocatedException;

/**
 * Java expression evaluation {@link PropertyResolver}.
 *
 * <p>
 * By default not recognized properties are silently ignored. To force to treat
 * the expression as Java prefix it with 'java:'.
 * </p>
 */
public class JaninoPropertyResolver implements PropertyResolver {
	public JaninoPropertyResolver() {
	}

	@Override
	public String getProperty(String name) {
		name = name.trim();
		boolean throwErrors = false;
		if (name.startsWith("java:")) {
			name = name.substring("java:".length());
			throwErrors = true;
		}
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		evaluator.setExpressionType(Object.class);
		try {
			evaluator.cook(name);
			return evaluator.evaluate(null).toString();
		} catch (LocatedException e) {
			if (!throwErrors) {
				return null;
			}
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if (!throwErrors) {
				return null;
			}
			throw new RuntimeException(e);
		}
	}
}
