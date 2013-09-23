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

import java.util.Collections;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat.ExistingStandardWrapper;
import org.atteo.evo.config.Configurable;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.webserver.ServletOrFilterDefinition;
import org.atteo.moonshine.webserver.ServletRegistry;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

/**
 * Context configuration.
 */
public class ContextConfig extends Configurable implements Service {
	@ImportService
	@XmlIDREF
	@XmlElement
	private ServletRegistry servletRegistry;

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
	 * Add {@link GuiceFilter}. When true servlets and filters registered using {@link ServletModule}s
	 * will be accessible under this context.
	 */
	@XmlElement
	private boolean registerGuiceFilter = false;

	public String getPath() {
		return path;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void configure(Context context) {
		for (ServletOrFilterDefinition<? extends Filter> filter : servletRegistry.getFilters()) {
			FilterDef filterDef = new FilterDef();
			filterDef.setFilter(filter.getProvider().get());
			filterDef.setFilterName(filter.getProviderClass().getName());
			context.addFilterDef(filterDef);

			FilterMap filterMap = new FilterMap();
			filterMap.setFilterName(filter.getProviderClass().getName());
			filterMap.addURLPattern(filter.getPrefix());

			context.addFilterMap(filterMap);
		}

		if (registerGuiceFilter) {
			FilterDef filterDef = new FilterDef();
			filterDef.setFilterName("guice-filter");
			filterDef.setFilterClass(GuiceFilter.class.getName());
			context.addFilterDef(filterDef);

			FilterMap filterMap = new FilterMap();
			filterMap.setFilterName("guice-filter");
			filterMap.addURLPattern("/*");
			context.addFilterMap(filterMap);
		}

		for (ServletOrFilterDefinition<? extends Servlet> servlet : servletRegistry.getServlets()) {
			Wrapper wrapper = new ExistingStandardWrapper(servlet.getProvider().get());
			wrapper.setName(servlet.getProviderClass().getName());
			for (Map.Entry<String, String> entry : servlet.getParams().entrySet()) {
				wrapper.addInitParameter(entry.getKey(), entry.getValue());
			}
			context.addChild(wrapper);
			context.addServletMapping(servlet.getPrefix(), servlet.getProviderClass().getName());
		}
	}

	@Override
	public Module configure() {
		return null;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void close() {
	}

	@Override
	public Iterable<? extends Service> getSubServices() {
		return Collections.emptyList();
	}
}
