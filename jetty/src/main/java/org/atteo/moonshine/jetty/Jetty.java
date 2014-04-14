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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.MBeanServer;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.ServiceConfiguration;
import org.atteo.moonshine.jetty.connectors.ConnectorConfig;
import org.atteo.moonshine.jetty.connectors.ServerConnectorConfig;
import org.atteo.moonshine.jetty.connectors.SslContextFactoryConfig;
import org.atteo.moonshine.jetty.handlers.HandlerCollectionConfig;
import org.atteo.moonshine.jetty.handlers.HandlerConfig;
import org.atteo.moonshine.jetty.handlers.HandlerListConfig;
import org.atteo.moonshine.jetty.handlers.ServletContextHandlerConfig;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.webserver.WebServerService;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * Starts Jetty web server instance.
 */
@XmlRootElement(name = "jetty")
@ServiceConfiguration(autoConfiguration = ""
		+ "<connectors>"
		+ "    <serverconnector>"
		+ "        <provideAddress>true</provideAddress>"
		+ "        <host>${oneof:${jetty.host},${webserver.host},}</host>"
		+ "        <port>${oneof:${jetty.port},${webserver.host},}</port>"
		+ "    </serverconnector>"
		+ "</connectors>")
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
		new ServerConnectorConfig(true)
	};

	/**
	 * Main handler. Usually a {@link HandlerCollectionConfig collection} or {@link HandlerListConfig list}
	 * of handlers. By default set to {@link ServletContextHandlerConfig}.
	 */
	@XmlElementRef
	private HandlerConfig handler = new ServletContextHandlerConfig();

	private static void addRecursively(List<Service> handlerServices, HandlerConfig handler) {
		if (handler instanceof Service) {
			handlerServices.add((Service) handler);
		}
		for (HandlerConfig subHandler : handler.getSubHandlers()) {
			addRecursively(handlerServices, subHandler);
		}
	}

	@Override
	public Iterable<? extends Service> getSubServices() {
		List<Service> handlerServices = new ArrayList<>();
		addRecursively(handlerServices, handler);

		return Iterables.concat(Arrays.asList(connectors), handlerServices);
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
