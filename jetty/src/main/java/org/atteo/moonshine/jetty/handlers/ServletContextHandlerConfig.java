/*
 * Copyright 2012 Atteo.
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
package org.atteo.moonshine.jetty.handlers;

import java.util.Collections;
import java.util.EnumSet;

import javax.inject.Provider;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.webserver.ServletOrFilterDefinition;
import org.atteo.moonshine.webserver.ServletRegistry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;


/**
 * Jetty servlet handler.
 * <p>
 * Handles servlets and filters registered using {@link ServletRegistry}
 * or using {@link ServletModule}.
 * </p>
 */
@XmlRootElement(name = "servlets")
public class ServletContextHandlerConfig extends HandlerConfig implements Service {
	@ImportService
	@XmlElement
	@XmlIDREF
	private ServletRegistry servletRegistry;

	/**
	 * Add {@link GuiceFilter}. When true servlets and filters registered using {@link ServletModule}s
	 * will be accessible under this context.
	 */
	@XmlElement
	private boolean registerGuiceFilter = false;

	private final ServletContextHandler handler = new ServletContextHandler();

	@Override
	public Handler getHandler() {
		for (ServletOrFilterDefinition<? extends Filter> filter : servletRegistry.getFilters()) {
			addFilter(filter);
		}

		for (ServletOrFilterDefinition<? extends Servlet> servlet : servletRegistry.getServlets()) {
			addServlet(servlet);
		}

		if (registerGuiceFilter) {
			handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
		}
		handler.addServlet(DefaultServlet.class, "/");

		return handler;
	}

	private <T extends Servlet> void addServlet(ServletOrFilterDefinition<T> servlet) {
		ProviderBasedServletHolder<T> holder =
				new ProviderBasedServletHolder<>(servlet.getProviderClass(), servlet.getProvider());
		holder.setInitParameters(servlet.getParams());
		handler.addServlet(holder, servlet.getPrefix());
	}

	private <T extends Filter> void addFilter(ServletOrFilterDefinition<T> filter) {
		ProviderBasedFilterHolder<T> holder =
				new ProviderBasedFilterHolder<>(filter.getProviderClass(), filter.getProvider());
		holder.setInitParameters(filter.getParams());
		handler.addFilter(holder, filter.getPrefix(), EnumSet.of(DispatcherType.REQUEST));
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

	private static class ProviderBasedServletHolder<T extends Servlet> extends ServletHolder {
		private Provider<T> provider;

		public ProviderBasedServletHolder(Class<T> servletClass, Provider<T> provider) {
			this.provider = provider;
			setClassName(servletClass.getName());
			setName(servletClass.getName() + "-" + this.hashCode());
		}

		@Override
		protected Servlet newInstance() throws ServletException, IllegalAccessException, InstantiationException {
			Servlet servlet = provider.get();
			provider = null;
			return servlet;
		}
	}

	private static class ProviderBasedFilterHolder<T extends Filter> extends FilterHolder {
		private Provider<T> provider;

		public ProviderBasedFilterHolder(Class<T> filterClass, Provider<T> provider) {
			this.provider = provider;
			setClassName(filterClass.getName());
		}

		@Override
		public void initialize() throws Exception {
			Filter filter = provider.get();
			provider = null;
			setFilter(filter);
			super.initialize();
		}
	}
}
