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
package org.atteo.evo.filtering;

import java.util.HashSet;
import java.util.Set;

/**
 * Recursively resolve properties.
 *
 * <p>
 * Property resolver which recursively resolves property name and property value using underlying property resolver.
 * For instance for defined underlying properties:
 * <ul>
 *   <li>name = "i"</li>
 *   <li>variable_i = "${constant}"</li>
 *   <li>constant = "value"</li>
 * </ul>
 * and recursive property ${variable_${name}}:
 * <ul>
 *   <li>property name 'variable_${name}' will be recursively resolved into 'variable_i'</li>
 *   <li>property name 'variable_i' will be resolved into value '${constant}'</li>
 *   <li>finally value '${constant}' will be recursively resolved into 'value'</li>
 * </ul>
 * Resolver algorithm returns error when circular dependency between properties is detected.
 * </p>
 */
public class RecursivePropertyResolver implements PropertyResolver {
	private PropertyResolver resolver;

	public RecursivePropertyResolver(PropertyResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public String getProperty(String name) {
		return new InternalResolver(resolver).getProperty(name);
	}

	private static class InternalResolver implements PropertyResolver {
		private PropertyResolver resolver;
		private Set<String> inProgress = new HashSet<String>();

		public InternalResolver(PropertyResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public String getProperty(String name) {
			if (inProgress.contains(name)) {
				throw new RuntimeException("Given property was resolved to itself creating"
						+ " circular dependency: " + name);
			}
			inProgress.add(name);

			String filteredName;

			try {
				filteredName = Filtering.filter(name, this);
			} catch (PropertyNotFoundException e) {
				throw new RuntimeException(e);
			}

			inProgress.add(filteredName);

			String value = resolver.getProperty(filteredName);
			if (value != null) {
				try {
					value = Filtering.filter(value, this);
				} catch (PropertyNotFoundException e) {
					throw new RuntimeException(e);
				}
			}

			inProgress.remove(filteredName);
			inProgress.remove(name);

			return value;
		}
	}
}
