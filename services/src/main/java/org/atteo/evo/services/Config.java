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
package org.atteo.evo.services;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.injection.InjectMembers;
import org.w3c.dom.Element;

/**
 * Root class for the {@link Services} configuration file.
 *
 * <p>
 * Root class wraps properties, the list of {@link Service}s and the list of {@link Group}s.
 * </p>
 */
@XmlRootElement
public class Config extends Group {
	@XmlElementWrapper(name = "properties")
	@XmlAnyElement(lax = false)
	private List<Element> properties;

	@InjectMembers
	@XmlElementRef
	@Valid
	private List<TopLevelService> services;

	@InjectMembers
	@XmlElementRef
	@Valid
	private List<TopLevelGroup> groups;

	@Override
	public List<Service> getServices() {
		List<Service> list = new ArrayList<Service>();
		if (services != null) {
			list.addAll(services);
		}

		if (groups != null) {
			for (Group group : groups) {
				list.addAll(group.getServices());
			}
		}
		return list;
	}
}
