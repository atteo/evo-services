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

import java.util.Collections;

import javax.servlet.ServletContainerInitializer;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.apache.catalina.Context;
import org.atteo.evo.config.Configurable;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.webserver.ServletContainer;

import com.google.inject.Module;

/**
 * Context configuration.
 */
public class ContextConfig extends Configurable implements Service {
	@ImportService
	@XmlIDREF
	@XmlElement
	private ServletContainer servletContainer;

	/**
	 * Context path.
	 */
	@XmlElement
	private String path = "/";

	/**
	 * Context base directory.
	 */
	@XmlElement
	private String baseDir = ".";

	public String getPath() {
		return path;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void configure(Context context) {
		for (ServletContainerInitializer servletContainerInitializer : servletContainer.getInitializers()) {
			context.addServletContainerInitializer(servletContainerInitializer, null);
		}
	}

	@Override
	public Module configure() {
		return null;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void close() {
	}

	@Override
	public Iterable<? extends Service> getSubServices() {
		return Collections.emptyList();
	}
}
