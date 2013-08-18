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

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import static org.apache.catalina.LifecycleState.STARTED;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.webserver.WebServerAddress;
import org.atteo.moonshine.webserver.WebServerService;

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
public class TomcatService extends WebServerService {
	/**
	 * Tomcat base directory.
	 */
	@XmlElement
	@XmlDefaultValue("${dataHome}/tomcat")
	private String baseDir;

	@XmlElementWrapper(name = "connectors")
	@XmlElement(name = "connector")
	private List<ConnectorConfig> connectors = new ArrayList<ConnectorConfig>() {
		private static final long serialVersionUID = 1L;

		{
			add(new ConnectorConfig());
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
						Context context = tomcat.addWebapp(host, contextConfig.getPath(), contextConfig.getBaseDir());
						for (FilterConfig filterConfig : contextConfig.getFilters()) {
							filterConfig.configure(context);
							requestInjection(filterConfig);
						}
					}

					tomcat.getEngine().addChild(host);
					tomcat.setHost(host);
				}

				for (ConnectorConfig connectorConfig : connectors) {
					Connector connector = new Connector(connectorConfig.getProtocol());
					connector.setPort(connectorConfig.getPort());

					tomcat.setConnector(connector);
					tomcat.getService().addConnector(connector);
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

	@Override
	public void start() {
		try {
			tomcat.start();
			if (tomcat.getConnector().getState() != STARTED) {
				throw new RuntimeException("Cannot start Tomcat");
			}
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
}
