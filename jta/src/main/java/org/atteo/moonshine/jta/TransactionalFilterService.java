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

package org.atteo.moonshine.jta;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.webserver.ServletContainer;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

@XmlRootElement(name = "transactional-filter")
public class TransactionalFilterService extends TopLevelService {
	@XmlIDREF
	@XmlElement
	@ImportService
	private TransactionalService transactional;

	@XmlIDREF
	@XmlElement
	@ImportService
	private ServletContainer servletContainer;

	/**
	 * Filter pattern matching urls for which {@link TransactionalFilter} will be registered.
	 */
	@XmlElement
	@XmlDefaultValue("/*")
	private String filterPattern;

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(TransactionalFilter.class);
				servletContainer.addFilter(getProvider(TransactionalFilter.class), filterPattern);
			}
		};
	}
}
