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

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.moonshine.ApplicationProperties;
import org.junit.ClassRule;
import org.junit.Test;

import com.google.inject.Key;

public class MoonshineRuleTest {
	@ClassRule
	public static final MoonshineRule moonshine = new MoonshineRule();

	@Test
	public void shouldProvideApplicationProperties() {
		// when
		PropertyResolver propertyResolver = moonshine.getGlobalInjector().getInstance(
				Key.get(PropertyResolver.class, ApplicationProperties.class));

		// then
		assertThat(propertyResolver).isNotNull();
	}
}
