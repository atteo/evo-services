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
package org.atteo.moonshine.jolokia;

import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.webserver.ServletRegistry;
import org.jolokia.http.AgentServlet;
import org.jolokia.restrictor.AllowAllRestrictor;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

/**
 * Jolokia - JMX-HTTP bridge.
 */
@XmlRootElement(name = "jolokia")
@Singleton
public class JolokiaService extends TopLevelService {
	@XmlElement
	@XmlIDREF
	@ImportService
	private ServletRegistry servletContainer;

	/**
	 * URL prefix under which Jolokia should be served.
	 */
	@XmlElement
	private String prefix = "/jolokia/*";

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(AgentServlet.class).toInstance(new AgentServlet(new AllowAllRestrictor()));
				servletContainer.addServlet(prefix, AgentServlet.class, getProvider(AgentServlet.class));
			}
		};
	}
}
