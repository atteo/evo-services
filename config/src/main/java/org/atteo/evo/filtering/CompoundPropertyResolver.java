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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Resolves properties by trying a number of underlying property resolvers.
 */
public class CompoundPropertyResolver implements PropertyResolver {
	private List<PropertyResolver> resolvers;

	public CompoundPropertyResolver(PropertyResolver... resolvers) {
		this.resolvers = Lists.newArrayList(resolvers);
	}

	public void addPropertyResolver(PropertyResolver resolver) {
		resolvers.add(resolver);
	}

	@Override
	public String getProperty(String name) {
		for (PropertyResolver resolver : resolvers) {
			String value = resolver.getProperty(name);
			if (value != null) {
				return value;
			}
		}
		return null;
	}
}