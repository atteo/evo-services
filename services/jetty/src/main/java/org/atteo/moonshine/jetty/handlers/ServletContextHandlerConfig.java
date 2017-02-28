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

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.webserver.ServletContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;


/**
 * Jetty servlet handler.
 * <p>
 * Handles servlets and filters registered using {@link ServletContainer}
 * or using {@link ServletModule}.
 * </p>
 */
@XmlRootElement(name = "servlets")
public class ServletContextHandlerConfig extends HandlerConfig implements Service {
	@ImportService
	@XmlElement
	@XmlIDREF
	private ServletContainer servletContainer;

	@Override
	public Handler getHandler() {
		final ServletContextHandler handler = new ServletContextHandler();
		handler.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
			@Override
			public void lifeCycleStarting(LifeCycle event) {
				handler.getServletContext().setExtendedListenerTypes(true);
				Iterable<ServletContainerInitializer> initializers = servletContainer.getInitializers();
				for (ServletContainerInitializer servletContainerInitializer : initializers) {
					try {
						servletContainerInitializer.onStartup(Collections.emptySet(),
								handler.getServletContext());
					} catch (ServletException ex) {
						throw new RuntimeException(ex);
					}
				}
				handler.addServlet(DefaultServlet.class, "/");
				handler.getServletContext().setExtendedListenerTypes(false);
			}
		});
		return handler;
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
