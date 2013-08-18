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

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.catalina.Context;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;

import com.google.inject.servlet.GuiceFilter;

/**
 * Registers {@link GuiceFilter}.
 */
@XmlRootElement(name = "guice-filter")
public class GuiceFilterConfig extends FilterConfig {
	private FilterDef filterDef = new FilterDef();

	@Override
	protected void configure(Context context) {
		filterDef.setFilterName("guice-filter");
		context.addFilterDef(filterDef);

		FilterMap filterMap = new FilterMap();
		filterMap.setFilterName("guice-filter");
		filterMap.addURLPattern("/*");

		context.addFilterMap(filterMap);
	}

	@Inject
	protected void setGuiceFilter(GuiceFilter filter) {
		filterDef.setFilter(filter);
	}
}
