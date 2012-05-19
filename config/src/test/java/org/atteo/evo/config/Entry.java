/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Entry extends Configurable {
	@XmlElement
	String value;

	@XmlElement(defaultValue = "5")
	int intValue;

	@XmlElement(defaultValue = "${intValue}")
	int intValue2;

	@XmlElement
	@XmlDefaultValue("${intValue}")
	Integer intValue3;

	@XmlElement
	@XmlDefaultValue("5")
	Integer intValue4;

	@XmlElement
	@XmlDefaultValue("true")
	Boolean booleanValue;

	public String getValue() {
		return value;
	}

	public int getIntValue() {
		return intValue;
	}

	public int getIntValue2() {
		return intValue2;
	}

	public int getIntValue3() {
		return intValue3;
	}

	public int getIntValue4() {
		return intValue4;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}
}
