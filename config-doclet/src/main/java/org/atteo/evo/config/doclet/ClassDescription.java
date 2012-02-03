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
package org.atteo.evo.config.doclet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;

import com.sun.javadoc.ClassDoc;

public class ClassDescription {
	private String tagName;
	private XmlAccessType accessType;
	private List<ElementDescription> attributes = new ArrayList<ElementDescription>();
	private List<ElementDescription> elements = new ArrayList<ElementDescription>();
	private String summary;
	private ClassDoc classDoc;

	public XmlAccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(XmlAccessType accessType) {
		this.accessType = accessType;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public List<ElementDescription> getElements() {
		return elements;
	}

	public List<ElementDescription> getAttributes() {
		return attributes;
	}

	public void addElement(ElementDescription element) {
		if (element.getType() == ElementType.ATTRIBUTE) {
			attributes.add(element);
		} else {
			elements.add(element);
		}
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public ClassDoc getClassDoc() {
		return classDoc;
	}

	public void setClassDoc(ClassDoc classDoc) {
		this.classDoc = classDoc;
	}
}
