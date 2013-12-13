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

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.Moonshine;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

@MoonshineConfiguration(configurator = ShouldCreateRequestPerMethodTest.Configurator.class)
public class ShouldCreateRequestPerMethodTest extends MoonshineTest {
	public static class Configurator implements MoonshineConfigurator {
		@Override
		public void configureMoonshine(Moonshine.Builder builder) {
			builder.addModule(new AbstractModule() {
				@Override
				protected void configure() {
					bind(Integer.class).toProvider(new Provider<Integer>() {
						private final AtomicInteger counter = new AtomicInteger();

						@Override
						public Integer get() {
							return counter.incrementAndGet();
						}
					}).in(RequestScoped.class);
				}
			});
		}
	}

	@Inject
	private Provider<Integer> value;

	private Integer oldValue = null;

	private void shouldResultInDifferentValues() {
		if (oldValue == null) {
			oldValue = value.get();
		} else {
			assertThat(value.get()).isNotEqualTo(oldValue);
		}
	}

	@Test
	public void firstMethod() {
		shouldResultInDifferentValues();
	}

	@Test public void secondMethod() {
		shouldResultInDifferentValues();
	}
}
