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
package org.atteo.moonshine.tomcat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import static org.apache.catalina.LifecycleState.STARTED;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.ServiceConfiguration;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.webserver.WebServerAddress;
import org.atteo.moonshine.webserver.WebServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

/**
 * Starts Tomcat.
 *
 * <p>
 * Tomcat support is currently very basic. You should choose Jetty most of the time.
 * </p>
 */
@XmlRootElement(name = "tomcat")
@ServiceConfiguration(autoConfiguration = ""
		+ "<connectors>"
		+ "    <connector>"
		+ "        <port>${oneof:${tomcat.port},${webserver.port},}</port>"
		+ "    </connector>"
		+ "</connectors>")
public class TomcatService extends WebServerService {
	/**
	 * Tomcat base directory.
	 */
	@XmlElement
	@XmlDefaultValue("${dataHome}/tomcat")
	private String baseDir;

	@XmlElementWrapper(name = "connectors")
	@XmlElement(name = "connector")
	private List<TomcatConnectorConfig> connectors = new ArrayList<TomcatConnectorConfig>() {
		private static final long serialVersionUID = 1L;

		{
			add(new TomcatConnectorConfig());
		}
	};

	/**
	 * Tomcat default host.
	 * <p>
	 * If not provided will be automatically set to the name of the first host in {@link #hosts}.
	 * </p>
	 */
	@XmlElement
	private String defaultHost = null;

	@XmlElementWrapper(name = "hosts")
	@XmlElement(name = "host")
	private List<HostConfig> hosts = new ArrayList<HostConfig>() {
		{
			add(new HostConfig());
		}
	};

	private Tomcat tomcat = null;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(GuiceFilter.class);

				try {
					Files.createDirectories(Paths.get(baseDir));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				bind(WebServerAddress.class).toInstance(new WebServerAddress() {
					@Override
					public int getPort() {
						return tomcat.getConnector().getLocalPort();
					}

					@Override
					public String getHost() {
						return tomcat.getHost().getName();
					}

					@Override
					public String getUrl() {
						String host = getHost();
						if (host == null) {
							host = "localhost";
						}
						return tomcat.getConnector().getScheme() + "://" + host + ":" + getPort();
					}
				});
			}
		};
	}

	private void init() {
		tomcat = new Tomcat();
		tomcat.setBaseDir(baseDir);
		if (defaultHost != null) {
			tomcat.getEngine().setDefaultHost(defaultHost);
		}

		for (HostConfig hostConfig : hosts) {
			StandardHost host = new StandardHost();
			host.setAppBase(hostConfig.getAppBase());
			host.setName(hostConfig.getName());
			if (defaultHost == null) {
				defaultHost = hostConfig.getName();
				tomcat.getEngine().setDefaultHost(defaultHost);
			}

			for (ContextConfig contextConfig : hostConfig.getContexts()) {
				Context context = tomcat.addContext(host, contextConfig.getPath(), contextConfig.getBaseDir());
				contextConfig.configure(context);
			}

			tomcat.getEngine().addChild(host);
			tomcat.setHost(host);
		}

		for (TomcatConnectorConfig connectorConfig : connectors) {
			Connector connector = new Connector(connectorConfig.getProtocol());
			connector.setPort(connectorConfig.getPort());

			tomcat.setConnector(connector);
			tomcat.getService().addConnector(connector);
		}
	}

	@Override
	public void start() {
		if (tomcat == null) {
			init();
		}
		try {
			tomcat.start();
			if (tomcat.getConnector().getState() != STARTED) {
				throw new RuntimeException("Cannot start Tomcat, check logs");
			}
			for (Container container : tomcat.getHost().findChildren()) {
				if (container.getState() != STARTED) {
					throw new RuntimeException("Cannot start Tomcat, check logs");
				}
			}
			printPorts();
		} catch (LifecycleException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		try {
			tomcat.stop();
		} catch (LifecycleException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterable<? extends Service> getSubServices() {
		List<Service> result = new ArrayList<>();
		for (TomcatConnectorConfig connector : connectors) {
			if (connector instanceof Service) {
				result.add((Service) connector);
			}
		}

		for (HostConfig host : hosts) {
			for (ContextConfig context : host.getContexts()) {
				if (context instanceof Service) {
					result.add(context);
				}
			}
		}
		return result;
	}

	private void printPorts() {
		Logger logger = LoggerFactory.getLogger("Moonshine");
		logger.info("    Tomcat started on {}:{}",tomcat.getHost().getName(),
				tomcat.getConnector().getLocalPort());
	}

}
