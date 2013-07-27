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
package org.atteo.moonshine;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.evo.filtering.PropertiesPropertyResolver;
import org.atteo.evo.filtering.PropertyResolver;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * {@link ServletContextListener} which starts {@link Moonshine} framework.
 *
 * <p>
 * Below is a sample web.xml which shows how to start Moonshine framework.
 * <pre>
 * {@code
 *
 * <web-app version="2.5" xmlns="http://java.sun.com/xml/ns/javaee"
 * 	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 * 	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
 *     metadata-complete="true">
 *
 * 	<context-param>
 *		<!-- Any context param will be available as a ${property} in Moonshine -->
 * 		<param-name>property name</param-name>
 * 		<param-value>property value</param-value>
 * 	</context-param>
 *
 * 	<listener>
 * 		<listener-class>org.atteo.moonshine.MoonshineServletContextListener</listener-class>
 * 	</listener>
 *
 * 	<filter>
 * 		<filter-name>guiceFilter</filter-name>
 * 		<filter-class>com.google.inject.servlet.GuiceFilter</filter-class>
 * 	</filter>
 *
 * 	<filter-mapping>
 * 		<filter-name>guiceFilter</filter-name>
 * 		<url-pattern>/*</url-pattern>
 * 	</filter-mapping>
 * </web-app>
 * }
 * </pre>
 * </p>
 */
public class MoonshineServletContextListener extends GuiceServletContextListener {
	private Moonshine moonshine;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		try {
			Moonshine.Builder builder = Moonshine.Factory.builder();

			configure(builder, servletContext);

			moonshine = builder.build();

			moonshine.start();
		} catch (IOException | IncorrectConfigurationException e) {
			throw new RuntimeException(e);
		}
		super.contextInitialized(servletContextEvent);
	}

	/**
	 * Configures {@link Moonshine} using provided builder.
	 * @param builder Moonshine builder
	 * @param servletContext servlet context
	 */
	protected void configure(Moonshine.Builder builder, ServletContext servletContext) {
		builder
				.applicationName(servletContext.getServletContextName())
				.shutdownHook(false)
				.addPropertyResolver(retrieveContextParameters(servletContext));
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		if (moonshine != null) {
			moonshine.close();
		}
	}

	@Override
	protected Injector getInjector() {
		return moonshine.getGlobalInjector();
	}

	private static PropertyResolver retrieveContextParameters(ServletContext servletContext) {
		Properties properties = new Properties();
		Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			String name = initParameterNames.nextElement();

			properties.put(name, servletContext.getInitParameter(name));
		}

		return new PropertiesPropertyResolver(properties);
	}
}
