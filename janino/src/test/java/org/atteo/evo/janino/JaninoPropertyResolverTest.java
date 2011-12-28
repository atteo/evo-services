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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class JaninoPropertyResolverTest {
	@Test
	public void simple() {
		JaninoPropertyResolver resolver = new JaninoPropertyResolver();
		String result = resolver.getProperty("new java.util.Date()");
		assertNotNull(result);

		result = resolver.getProperty("3+3");
		assertEquals("6", result);
	}

	@Test(expected = RuntimeException.class)
	public void forced() {
		JaninoPropertyResolver resolver = new JaninoPropertyResolver();
		resolver.getProperty("java: asdf");
	}
}
