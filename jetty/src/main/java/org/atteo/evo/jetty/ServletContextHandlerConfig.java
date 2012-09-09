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
package org.atteo.evo.jetty;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;

/**
 * Handles servlets and filters registered in Guice using {@link ServletModule}. 
 */
@XmlRootElement(name = "servlets")
public class ServletContextHandlerConfig extends HandlerConfig {
	@Override
	public Handler getHandler() {
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
		handler.addServlet(DefaultServlet.class, "/");
		
		return handler;
	}
}
