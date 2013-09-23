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
package org.atteo.moonshine.webserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;

/**
 * Allows configuration of {@link Servlet servlets} and {@link Filter filters}.
 */
@XmlRootElement(name = "servlet-registry")
public class ServletRegistry extends TopLevelService {
	private final List<ServletOrFilterDefinition<? extends Servlet>> servlets = new ArrayList<>();
	private final List<ServletOrFilterDefinition<? extends Filter>> filters = new ArrayList<>();

	/**
	 * Register servlet.
	 * @param prefix URL prefix to register servlet onto
	 * @param servlet servlet
	 */
	public <T extends Servlet> void addServlet(String prefix, Class<T> servletClass, T servlet) {
		addServlet(prefix, servletClass, servlet, Collections.<String, String>emptyMap());
	}

	/**
	 * Register servlet.
	 * @param prefix URL prefix to register servlet onto
	 * @param servlet servlet provider
	 */
	public <T extends Servlet> void addServlet(String prefix, Class<T> servletClass, Provider<T> servlet) {
		addServlet(prefix, servletClass, servlet, Collections.<String, String>emptyMap());
	}

	/**
	 * Register servlet.
	 * @param prefix URL prefix to register servlet onto
	 * @param servletClass servlet class
	 * @param servlet servlet
	 * @param params servlet init parameters
	 */
	public <T extends Servlet> void addServlet(String prefix, Class<T> servletClass, final T servlet,
			Map<String, String> params) {
		Provider<T> provider = new Provider<T>() {
			@Override
			public T get() {
				return servlet;
			}
		};
		servlets.add(new ServletOrFilterDefinition<>(prefix, servletClass, provider, params));
	}

	/**
	 * Register servlet.
	 * @param prefix URL prefix to register servlet onto
	 * @param servletClass servlet class
	 * @param servlet servlet provider
	 * @param params servlet init parameters
	 */
	public <T extends Servlet> void addServlet(String prefix, Class<T> servletClass, Provider<T> servlet,
			Map<String, String> params) {
		servlets.add(new ServletOrFilterDefinition<>(prefix, servletClass, servlet, params));
	}

	/**
	 * Register filter.
	 * @param prefix URL prefix to register filter onto
	 * @param filter filter
	 */
	public <T extends Filter> void addFilter(String prefix, Class<T> filterClass, T filter) {
		addFilter(prefix, filterClass, filter, Collections.<String, String> emptyMap());
	}

	/**
	 * Register filter.
	 * @param prefix URL prefix to register filter onto
	 * @param filter filter provider
	 */
	public <T extends Filter> void addFilter(String prefix, Class<T> filterClass, Provider<T> filter) {
		addFilter(prefix, filterClass, filter, Collections.<String, String> emptyMap());
	}

	/**
	 * Register filter.
	 * @param prefix URL prefix to register filter onto
	 * @param filterClass filter class
	 * @param filter filter
	 * @param params filter init parameters
	 */
	public <T extends Filter> void addFilter(String prefix, Class<T> filterClass, final T filter,
			Map<String, String> params) {
		Provider<T> provider = new Provider<T>() {
			@Override
			public T get() {
				return filter;
			}
		};
		filters.add(new ServletOrFilterDefinition<>(prefix, filterClass, provider, params));
	}

	/**
	 * Register filter.
	 * @param prefix URL prefix to register filter onto
	 * @param filterClass filter class
	 * @param filter filter provider
	 * @param params filter init parameters
	 */
	public <T extends Filter> void addFilter(String prefix, Class<T> filterClass, Provider<T> filter,
			Map<String, String> params) {
		filters.add(new ServletOrFilterDefinition<>(prefix, filterClass, filter, params));
	}

	public List<ServletOrFilterDefinition<? extends Servlet>> getServlets() {
		return servlets;
	}

	public List<ServletOrFilterDefinition<? extends Filter>> getFilters() {
		return filters;
	}
}
