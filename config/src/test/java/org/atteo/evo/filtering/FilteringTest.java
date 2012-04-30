/*
 * Copyright 2012 Atteo.
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
package org.atteo.evo.filtering;

import java.util.List;

import org.atteo.evo.filtering.Filtering.Part;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FilteringTest {
	@Test
	public void simple() {
		List<Part> parts = Filtering.splitIntoParts("${a}");
		assertEquals(1, parts.size());
		assertEquals(true, parts.get(0).isProperty());
	}

	@Test
	public void nested() {
		List<Part> parts = Filtering.splitIntoParts("${a_${b}}");
		assertEquals(1, parts.size());
		assertEquals(true, parts.get(0).isProperty());
	}
}
