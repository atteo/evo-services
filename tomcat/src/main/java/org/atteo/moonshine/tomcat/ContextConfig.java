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
package org.atteo.moonshine.tomcat;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.atteo.evo.config.Configurable;

/**
 * Context configuration.
 */
public class ContextConfig extends Configurable {
	/**
	 * Context path.
	 */
	@XmlElement
	private String path = "/";

	/**
	 * Context base directory.
	 */
	@XmlElement
	private String baseDir = ".";

	/**
	 * Filters
	 */
	@XmlElementWrapper(name = "filters")
	@XmlElementRef
	List<FilterConfig> filters = new ArrayList<FilterConfig>() {
		private static final long serialVersionUID = 1L;

		{
			add(new GuiceFilterConfig());
		}
	};

	public String getPath() {
		return path;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public List<FilterConfig> getFilters() {
		return filters;
	}
}
