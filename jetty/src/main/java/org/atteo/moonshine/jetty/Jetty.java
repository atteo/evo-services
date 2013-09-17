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
package org.atteo.moonshine.jetty;

import java.util.Arrays;

import javax.management.MBeanServer;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.webserver.WebServerService;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;

import com.google.inject.Inject;
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
 * to this Jetty service only when it was compiled with webserver.jar in the classpath
 * which contains annotation processor generating the needed class index.
 * Use {@link ServletModule} when that is not the case.
 * </p>
 */
@XmlRootElement(name = "jetty")
public class Jetty extends WebServerService {
	@XmlElementWrapper(name = "sslcontextfactories")
	@XmlElementRef
	private SslContextFactoryConfig[] sslContextFactories;


	/**
	 * List of connectors.
	 */
	@XmlElementWrapper(name = "connectors")
	@XmlElementRef
	private ConnectorConfig[] connectors = new ConnectorConfig[] {
		new ServerConnectorConfig()
	};

	/**
	 * Main handler. Usually a {@link HandlerCollectionConfig collection} or {@link HandlerListConfig list}
	 * of handlers. By default set to {@link ServletContextHandlerConfig}.
	 */
	@XmlElementRef
	private HandlerConfig handler = new ServletContextHandlerConfig();

	@Override
	public Iterable<? extends Service> getSubServices() {
		return Arrays.asList(connectors);
	}

	@Inject(optional = true)
	private MBeanServer mbeanServer;

	private Server server;

	@Override
	public void start() {
		server = new Server();

		server.setHandler(handler.getHandler());

		for (ConnectorConfig config : connectors) {
			server.addConnector(config.getConnector(server));
		}

		if (mbeanServer != null) {
			MBeanContainer mbContainer = new MBeanContainer(mbeanServer);
			server.addBean(mbContainer);
			server.addBean(Log.getLog());
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
	public void close() {
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
