/*
 * Copyright 2014 Atteo.
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

package org.atteo.moonshine.webjars;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.webserver.ServletContainer;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

@XmlRootElement(name = "webjars")
public class WebJarsService extends TopLevelService {
	@ImportService
	private ServletContainer servletContainer;

	@XmlElement
	private String prefix = "/webjars";

	@XmlElement
	private String destination = "/META-INF/resources/webjars";

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(WebJarsServlet.class).toInstance(new WebJarsServlet(destination));

				servletContainer.addServlet(getProvider(WebJarsServlet.class), prefix + "/*");
				
			}
		};
	}

}
