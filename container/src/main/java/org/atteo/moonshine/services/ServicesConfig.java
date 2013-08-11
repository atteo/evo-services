/*
 * Copyright 2013 Atteo.
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
package org.atteo.moonshine.services;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElementRef;

import com.google.common.collect.Iterables;

/**
 * Root class for the {@link Services} configuration file.
 */
public class ServicesConfig extends Service {
	@XmlElementRef
	@Valid
	private List<TopLevelService> services;

	@Override
	public List<Service> getSubServices() {
		List<Service> list = new ArrayList<>();
		if (services != null) {
			list.addAll(services);
			for (Service service : services) {
				Iterables.addAll(list, service.getSubServices());
			}
		}

		return list;
	}
}
