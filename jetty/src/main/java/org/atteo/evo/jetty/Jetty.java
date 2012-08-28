/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.jetty;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.services.ContentDirectory;
import org.atteo.evo.services.ExternalContainer;
import org.atteo.evo.services.TopLevelService;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

/**
 * Starts embedded Jetty web server instance.
 * 
 * <p>
 * There are two ways to register servlets and filters:
 * <ul>
 * <li>by annotating {@link HttpServlet servlet} with &#064;{@link WebServlet}
 * and {@link Filter filter} with &#064;{@link WebFilter}</li>
 * <li>by registering {@link ServletModule} in Guice</li>
 * </ul>
 * </p>
 * <p>
 * Please note that when using annotations the {@link ClassIndex} facility
 * is used to get the list of annotated classes as opposed to otherwise more common
 * classpath scanning. The implication of that is that the class will be "visible"
 * to this Jetty service only when it was compiled with evo-classindex.jar in the classpath
 * which contains annotation processor generating the needed class index.
 * Use {@link ServletModule} when that is not the case.
 * </p>
 */
@XmlRootElement(name = "jetty")
public class Jetty extends TopLevelService {
	/**
	 * Port on which the server will listen.
	 */
	@XmlElement
	private Integer port = 8080;

	/**
	 * Default page to open.
	 */
	@XmlElement
	private String welcomeFile = "Application.html";

	@Override
	public Module configure() {
		return new ServletModule() {
			@Override
			protected void configureServlets() {
				for (Class<?> klass : ClassIndex.getAnnotated(WebServlet.class)) {
					WebServlet annotation = klass.getAnnotation(WebServlet.class);
					String[] urls = annotation.value();
					if (urls.length == 0) {
						urls = annotation.urlPatterns();
					}

					Map<String, String> params = new HashMap<String, String>();

					for (WebInitParam param : annotation.initParams()) {
						params.put(param.name(), param.value());
					}

					String[] restOfUrls = Arrays.copyOfRange(urls, 1, urls.length - 1);
					@SuppressWarnings("unchecked")
					Class<? extends HttpServlet> servletClass = (Class<? extends HttpServlet>) klass;
					serve(urls[0], restOfUrls).with(servletClass, params);
					bind(servletClass).in(Singleton.class);
				}
				for (Class<?> klass : ClassIndex.getAnnotated(WebFilter.class)) {
					WebFilter annotation = klass.getAnnotation(WebFilter.class);
					String[] urls = annotation.value();
					if (urls.length == 0) {
						urls = annotation.urlPatterns();
					}

					Map<String, String> params = new HashMap<String, String>();

					for (WebInitParam param : annotation.initParams()) {
						params.put(param.name(), param.value());
					}

					String[] restOfUrls = Arrays.copyOfRange(urls, 1, urls.length - 1);
					@SuppressWarnings("unchecked")
					Class<? extends Filter> filterClass = (Class<? extends Filter>) klass;
					filter(urls[0], restOfUrls).through(filterClass, params);
					bind(filterClass).in(Singleton.class);
				}
			}
		};
	}

	@Inject
	private Injector injector;

	@Inject(optional = true)
	private MBeanServer mbeanServer;

	@Inject
	@ExternalContainer
	private Boolean externalContainer;

	@Inject(optional = true)
	// not set when in external container
	@ContentDirectory
	private File warDirectory;

	private Server server;

	@SuppressWarnings("deprecation")
	@Override
	public void start() {
		if (externalContainer) {
			return;
		}
		server = new Server();

		ServletContextHandler servlets = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servlets.setResourceBase(warDirectory.getAbsolutePath());

		servlets.addFilter(GuiceFilter.class, "/*", EnumSet.noneOf(DispatcherType.class));
		servlets.addEventListener(new GuiceServletContextListener() {
			@Override
			protected Injector getInjector() {
				return injector;
			}
		});
		servlets.addServlet(DefaultServlet.class, "/");

		ResourceHandler resources = new ResourceHandler();
		resources.setWelcomeFiles(new String[] { welcomeFile });
		resources.setResourceBase(warDirectory.getAbsolutePath());

		HandlerList handlers = new HandlerList();
		handlers.addHandler(resources);
		handlers.addHandler(servlets);
		server.setHandler(handlers);

		Connector connector = new BlockingChannelConnector();
		connector.setPort(port);
		server.addConnector(connector);

		if (mbeanServer != null) {
			MBeanContainer mbContainer = new MBeanContainer(mbeanServer);
			server.getContainer().addEventListener(mbContainer);
			server.addBean(mbContainer);
			mbContainer.addBean(Log.getLog());
		}

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		if (server == null) {
			return;
		}
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
