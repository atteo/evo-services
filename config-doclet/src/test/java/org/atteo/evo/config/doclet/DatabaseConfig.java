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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@XmlRootElement(name = "database")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class DatabaseConfig extends ConfigEntry {
	/**
	 * Database URL
	 */
	public String url = "default";

	@XmlTransient
	public int test = -1;

	@XmlElement(name = "table")
	private List<String> tables;

	@XmlElementWrapper(name = "otherTables")
	@XmlElement(name = "table")
	private List<String> otherTables;
}
