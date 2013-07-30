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
package org.atteo.moonshine.services.assertions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Assertion which tests two values for equality.
 */
@XmlRootElement(name = "equals")
public class EqualsAssertion extends Assertion {
	/**
	 * Expected value.
	 */
	@XmlElement
	private String expected;

	/**
	 * Actual value.
	 */
	@XmlElement
	private String actual;

	@Override
	public void check() {
		if (expected == null) {
			if (actual == null) {
				return;
			}
		} else {
			if (expected.equals(actual)) {
				return;
			}
		}
		throw new AssertionError("Values not equal, expected: " + expected
				+ ", actual: " + actual);
	}
}
