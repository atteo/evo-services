/*
 * Copyright 2013 Atteo.
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

import org.atteo.evo.filtering.PropertyFilter;
import org.atteo.evo.filtering.PropertyNotFoundException;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.moonshine.Moonshine;

public class SimpleConfigurator implements MoonshineConfigurator {
	@Override
	public void configureMoonshine(Moonshine.Builder builder) {
		builder.addConfigurationFromString(""
				+ "<config>"
				+ "    <simple message = '${message}'/>"
				+ "</config>");
		builder.addPropertyResolver(new PropertyResolver() {
			@Override
			public String resolveProperty(String property, PropertyFilter pr) throws PropertyNotFoundException {
				if ("message".equals(property)) {
					return "Hello World";
				}
				return null;
			}
		});
	}
}
