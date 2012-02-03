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

import com.sun.javadoc.Type;

public class ElementDescription {
	private String name;
	private String comment;
	private boolean required;
	private ElementType type;
	private String defaultValue;
	private boolean collection;
	private String wrapperName;
	private Type elementType;
	private boolean idref = false;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		if (comment != null) {
			comment = comment.trim();
			if (comment.isEmpty()) {
				comment = null;
			}
		}
		this.comment = comment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public ElementType getType() {
		return type;
	}

	public void setType(ElementType type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isCollection() {
		return collection;
	}

	public void setCollection(boolean collection) {
		this.collection = collection;
	}

	public String getWrapperName() {
		return wrapperName;
	}

	public void setWrapperName(String wrapperName) {
		this.wrapperName = wrapperName;
	}

	public Type getElementType() {
		return elementType;
	}
	
	public void setElementType(Type elementType) {
		this.elementType = elementType;
	}

	public boolean isIdref() {
		return idref;
	}

	public void setIdref(boolean idref) {
		this.idref = idref;
	}
}
