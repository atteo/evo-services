/*
 * Copyright 2014 Atteo.
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

package org.atteo.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

import org.atteo.config.xmlmerge.CombineChildren;
import org.atteo.config.xmlmerge.CombineSelf;

public class AbstractConfigurable implements Configurable {
	private CombineSelf combineSelf;
	private CombineChildren combineChildren;
	private String idValue;

	@Override
	public CombineSelf getCombineSelf() {
		return combineSelf;
	}

	@Override
	public void setCombineSelf(CombineSelf combineSelf) {
		this.combineSelf = combineSelf;
	}

	@Override
	public CombineChildren getCombineChildren() {
		return combineChildren;
	}

	@Override
	public void setCombineChildren(CombineChildren combineChildren) {
		this.combineChildren = combineChildren;
	}

	@XmlID
	@XmlAttribute(name = "id")
	@Override
	public String getId() {
		return idValue;
	}

	@Override
	public void setId(String id) {
		this.idValue = id;
	}
}
