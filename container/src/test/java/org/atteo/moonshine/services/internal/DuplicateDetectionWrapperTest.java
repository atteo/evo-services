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

package org.atteo.moonshine.services.internal;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.spi.Elements;

public class DuplicateDetectionWrapperTest {
	private interface Interface {
	}
	private static class Module1 extends AbstractModule {
		@Override
		public void configure() {
			bind(Interface.class).toInstance(new Interface(){});
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Module1;
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	@Test(expected = CreationException.class)
	public void shouldFailWithoutDuplicateDetector() {
		Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				install(new Module1());
				install(new Module1()); // Fine without duplicate detector
				install(Elements.getModule(Elements.getElements(new Module1()))); // Problem
			}
		});
	}

	@Test
	public void shouldRemoveDuplicates() {
		final DuplicateDetectionWrapper duplicateDetection = new DuplicateDetectionWrapper();

		Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				install(duplicateDetection.wrap(new Module1()));
				install(duplicateDetection.wrap(new Module1())); // Fine without duplicate detector
				install(Elements.getModule(Elements.getElements(duplicateDetection.wrap(new Module1()))));
			}
		});
	}
}
