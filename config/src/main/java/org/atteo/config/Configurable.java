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
package org.atteo.config;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

import org.atteo.classindex.IndexSubclasses;
import org.atteo.xmlcombiner.CombineChildren;
import org.atteo.xmlcombiner.CombineSelf;

/**
 * Any configurable element from the configuration file.
 *
 * <p>
 * Configurable contains an 'id' attribute which can be referenced
 * from other places using standard ID/IDREF functionality of the XML schema.
 * </p>
 */
@IndexSubclasses
@XmlAccessorType(XmlAccessType.NONE)
public interface Configurable {
	/**
	 * Returns an ID for this element associated in the XML file.
	 * @return an ID or null, if ID not assigned
	 */
	@XmlID
	@XmlAttribute(name = "id")
	@Pattern(regexp = "\\w[\\w\\d]*")
	public String getId();

	/**
	 * Id by which this element can be referenced.
	 */
	public void setId(String id);

	/**
	 * Describes the behavior of node combining when merging XML trees.
	 */
	@XmlAttribute(name = CombineSelf.ATTRIBUTE_NAME)
	CombineSelf getCombineSelf();

	void setCombineSelf(CombineSelf combineSelf);

	/**
	 * Describes the behavior of children combining when merging XML trees.
	 */
	@XmlAttribute(name = CombineChildren.ATTRIBUTE_NAME)
	CombineChildren getCombineChildren();

	void setCombineChildren(CombineChildren combineChildren);
}
