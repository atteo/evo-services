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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.services.TopLevelService;
import org.jolokia.http.AgentServlet;
import org.jolokia.restrictor.AllowAllRestrictor;

import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

/**
 * Jolokia - JMX-HTTP bridge.
 */
@XmlRootElement(name = "jolokia")
public class JolokiaService extends TopLevelService {
	/**
	 * URL prefix under which Jolokia should be served.
	 */
	@XmlElement
	private String prefix = "/jolokia/*";

	@Override
	public Module configure() {
		return new ServletModule() {
			@Override
			protected void configureServlets() {
				bind(AgentServlet.class).toInstance(new AgentServlet(new AllowAllRestrictor()));
				serve(prefix).with(AgentServlet.class);
			}
		};
	}
}