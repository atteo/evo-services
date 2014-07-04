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
package org.atteo.moonshine;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.services.Service;
import org.w3c.dom.Element;

/**
 * Root class for the Moonshine configuration file.
 */
@XmlRootElement
public class Config extends ConfigurableService {
	@XmlElementWrapper(name = "properties")
	@XmlAnyElement(lax = false)
	private List<Element> properties;

	@XmlElementRef
	@Valid
	private List<TopLevelService> services = new ArrayList<>();

	@Override
	public List<? extends Service> getSubServices() {
		return services;
	}
}
