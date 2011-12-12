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

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

import org.atteo.evo.classindex.IndexSubclasses;

/**
 * Any configurable element from the configuration file.
 *
 * Configurable contains an 'id' attribute which can be referenced
 * from other places using standard ID/IDREF functionality of the XML schema.
 */
@IndexSubclasses
public abstract class Configurable {
	/**
	 * Id by which this element can be referenced.
	 */
	@XmlID
	@XmlAttribute
	@Pattern(regexp = "\\w[\\w\\d]*")
	private String id;

	/**
	 * Describes the behavior during the XML combine process.
	 */
	@XmlAttribute
	private Combine combine = Combine.MERGE;

	/**
	 * Returns an ID for this element associated in the XML file.
	 * @return an ID or null, if ID not assigned
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns behavior for the node during the XML combine process.
	 * @return node behavior
	 */
	public Combine getCombine() {
		return combine;
	}
}
