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
import java.util.EnumSet;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;

import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;

/**
 * Allows configuration of servlets, filters and listeners.
 */
@XmlRootElement(name = "servlet-container")
public class ServletContainer extends TopLevelService {

	/**
	 * Default priority assigned to filters.
	 */
	public static final int DEFAULT_PRIORITY = 0;

	/**
	 * Add {@link GuiceFilter}. When true servlets and filters registered
	 * using {@link ServletModule}s will be accessible under this context.
	 */
	@XmlElement
	private boolean registerGuiceFilter = false;

	private final List<ServletOrFilterDefinition<? extends Servlet>> servlets = new ArrayList<>();

	private final List<ServletOrFilterDefinition<? extends Filter>> filters = new ArrayList<>();

	private final List<Provider<? extends EventListener>> listeners = new ArrayList<>();

	private final List<ServletContainerInitializer> initializers = new ArrayList<>();

	public ServletContainer() {
		initializers.add(new Initializer());
	}

	/**
	 * Register servlet.
	 *
	 * @param patterns URL pattern to register servlet onto
	 * @param servlet servlet
	 */
	public <T extends Servlet> void addServlet(T servlet, String... patterns) {
		addServlet(servlet, Collections.<String, String>emptyMap(), patterns);
	}

	/**
	 * Register servlet.
	 *
	 * @param patterns URL pattern to register servlet onto
	 * @param servlet servlet provider
	 */
	public <T extends Servlet> void addServlet(Provider<T> servlet, String... patterns) {
		addServlet(servlet, Collections.<String, String>emptyMap(), patterns);
	}

	/**
	 * Register servlet.
	 *
	 * @param patterns URL pattern to register servlet onto
	 * @param servlet servlet
	 * @param params servlet init parameters
	 */
	public <T extends Servlet> void addServlet(final T servlet, Map<String, String> params, String... patterns) {
		Provider<T> provider = () -> servlet;
		servlets.add(new ServletOrFilterDefinition<>(provider, params, patterns, DEFAULT_PRIORITY));
	}

	/**
	 * Register servlet.
	 *
	 * @param patterns URL pattern to register servlet onto
	 * @param servlet servlet provider
	 * @param params servlet init parameters
	 */
	public <T extends Servlet> void addServlet(Provider<T> servlet, Map<String, String> params, String... patterns) {
		servlets.add(new ServletOrFilterDefinition<>(servlet, params, patterns, DEFAULT_PRIORITY));
	}

	/**
	 * Register filter. It will be assigned the default priority.
	 *
	 * @param patterns URL pattern to register filter onto
	 * @param filter filter
	 */
	public <T extends Filter> void addFilter(T filter, String patterns) {
		addFilter(filter, Collections.<String, String>emptyMap(), DEFAULT_PRIORITY, patterns);
	}

	/**
	 * Register filter. It will be assigned the default priority.
	 *
	 * @param patterns URL pattern to register filter onto
	 * @param filter filter provider
	 */
	public <T extends Filter> void addFilter(Provider<T> filter, String... patterns) {
		addFilter(filter, Collections.<String, String>emptyMap(), DEFAULT_PRIORITY, patterns);
	}

	/**
	 * Register filter.
	 *
	 * @param patterns URL pattern to register filter onto
	 * @param filter filter
	 * @param params filter init parameters
	 */
	public <T extends Filter> void addFilter(final T filter, Map<String, String> params, String... patterns) {
		addFilter(filter, params, DEFAULT_PRIORITY, patterns);
	}

	/**
	 * Register filter.
	 *
	 * @param patterns URL pattern to register filter onto
	 * @param filter filter provider
	 * @param params filter init parameters
	 */
	public <T extends Filter> void addFilter(Provider<T> filter, Map<String, String> params, String... patterns) {
		addFilter(filter, params, DEFAULT_PRIORITY, patterns);
	}

	/**
	 * Register filter.
	 *
	 * @param patterns URL pattern to register filter onto
	 * @param filter filter
	 * @param priority filter priority, filters with lower priorities are
	 * executed first
	 * @param params filter init parameters
	 */
	public <T extends Filter> void addFilter(final T filter, Map<String, String> params, int priority, String... patterns) {
		Provider<T> provider = () -> filter;
		filters.add(new ServletOrFilterDefinition<>(provider, params, patterns, priority));
	}

	/**
	 * Register filter.
	 *
	 * @param patterns URL pattern to register filter onto
	 * @param filter filter provider
	 * @param priority filter priority, filters with lower priorities are
	 * executed first
	 * @param params filter init parameters
	 */
	public <T extends Filter> void addFilter(Provider<T> filter, Map<String, String> params, int priority, String... patterns) {
		filters.add(new ServletOrFilterDefinition<>(filter, params, patterns, priority));
	}

	/**
	 * Register listener.
	 *
	 * @param listener listener provider
	 */
	public <T extends EventListener> void addListener(Provider<T> listener) {
		listeners.add(listener);
	}

	/**
	 * Register listener.
	 *
	 * @param listener listener to register
	 */
	public <T extends EventListener> void addListener(final T listener) {
		listeners.add((Provider<T>) () -> listener);
	}

	public void addServletContainerInitializer(ServletContainerInitializer initializer) {
		initializers.add(initializer);
	}

	public Iterable<ServletContainerInitializer> getInitializers() {
		return initializers;
	}

	private class Initializer implements ServletContainerInitializer {

		@Override
		public void onStartup(Set<Class<?>> c, ServletContext context) throws ServletException {
			if (registerGuiceFilter) {
				FilterRegistration.Dynamic registration = context.addFilter("guice-filter", GuiceFilter.class);
				registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
			}

			// sort filters by priority
			Collections.sort(filters);

			int counter = 0;
			for (ServletOrFilterDefinition<? extends Filter> filter : filters) {
				String name = "filter" + counter;
				counter++;
				FilterRegistration.Dynamic registration = context.addFilter(name, filter.getProvider().get());
				registration.setInitParameters(filter.getParams());
				registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, filter.getPatterns());
			}

			counter = 0;
			for (ServletOrFilterDefinition<? extends Servlet> servlet : servlets) {
				String name = "servlet" + counter;
				counter++;
				ServletRegistration.Dynamic registration = context.addServlet(name, servlet.getProvider().get());
				registration.setInitParameters(servlet.getParams());
				registration.addMapping(servlet.getPatterns());
			}

			for (Provider<? extends EventListener> listener : listeners) {
				context.addListener(listener.get());
			}
		}
	}
}
