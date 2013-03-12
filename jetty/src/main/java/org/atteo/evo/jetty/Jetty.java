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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.services.ExternalContainer;
import org.atteo.evo.services.TopLevelService;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
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
	 * Support {@link WebServlet}, {@link WebFilter} and {@link WebInitParam} annotations
	 * by automatically registering annotated classes using {@link ServletModule}.
	 */
	@XmlElement
	private boolean registerAnnotatedServlets = true;

	/**
	 * List of connectors.
	 */
	@XmlElementWrapper(name = "connectors")
	@XmlElementRef
	private ConnectorConfig[] connectors = new ConnectorConfig[] {
		new BlockingChannelConnectorConfig()
	};

	/**
	 * Main handler. Usually a {@link HandlerCollectionConfig collection} or {@link HandlerListConfig list}
	 * of handlers. By default set to {@link ServletContextHandlerConfig}.
	 */
	@XmlElementRef
	private HandlerConfig handler = new ServletContextHandlerConfig();

	@Override
	public Module configure() {
		return new ServletModule() {
			@Override
			protected void configureServlets() {
				for (ConnectorConfig connector : connectors) {
					Module module = connector.configure();
					if (module != null) {
						install(module);
					}
				}

				Module module = handler.configure();
				if (module != null) {
					install(module);
				}

				if (!registerAnnotatedServlets) {
					return;
				}
				for (Class<?> klass : ClassIndex.getAnnotated(WebServlet.class)) {
					WebServlet annotation = klass.getAnnotation(WebServlet.class);
					String[] urls = annotation.value();
					if (urls.length == 0) {
						urls = annotation.urlPatterns();
					}

					Map<String, String> params = new HashMap<>();

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

					Map<String, String> params = new HashMap<>();

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

	@Inject(optional = true)
	private MBeanServer mbeanServer;

	@Inject
	@ExternalContainer
	private Boolean externalContainer;

	private Server server;

	@SuppressWarnings("deprecation")
	@Override
	public void start() {
		if (externalContainer) {
			return;
		}
		server = new Server();

		server.setHandler(handler.getHandler());

		for (ConnectorConfig config : connectors) {
			server.addConnector(config.getConnector());
		}

		if (mbeanServer != null) {
			MBeanContainer mbContainer = new MBeanContainer(mbeanServer);
			server.getContainer().addEventListener(mbContainer);
			server.addBean(mbContainer);
			mbContainer.addBean(Log.getLog());
		}

		try {
			server.start();
		} catch (RuntimeException e) {
			throw e;
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
