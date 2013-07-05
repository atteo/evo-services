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
package org.atteo.moonshine.database;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.injection.InjectMembers;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.services.TopLevelGroup;

@XmlRootElement(name = "databases")
@XmlAccessorType(XmlAccessType.NONE)
public class DatabasesList extends TopLevelGroup {
	@XmlElementRef
	@InjectMembers
	List<DatabaseService> databases;

	@Override
	public List<? extends Service> getServices() {
		return Collections.unmodifiableList(databases);
	}
}
