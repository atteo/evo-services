/*
 * Copyright 2012 Atteo.
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
package org.atteo.evo.jetty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * Serves static content and handles If-Modified-Since headers.
 * No caching is done. Requests for resources that do not exist
 * are let pass (Eg no 404's).
 *
 * @see ResourceHandler
 */
@XmlRootElement(name = "resource")
public class ResourceHandlerConfig extends HandlerConfig {
	@XmlElement
	private boolean directoriesListed = false;

	@XmlElementWrapper(name = "welcomeFiles")
	@XmlElement(name = "welcomeFile")
	private String[] welcomeFiles = {"index.html"};

	@XmlElementWrapper(name = "mimeTypes")
	@XmlElementRef
	private ResourceHandlerMimeTypeConfig[] mimeTypes = {};

	@XmlElement
	private String resourceBase;

	@Override
	public Handler getHandler() {
		ResourceHandler handler = new ResourceHandler();
		handler.setDirectoriesListed(directoriesListed);
		handler.setWelcomeFiles(welcomeFiles);

		for (ResourceHandlerMimeTypeConfig mimeType : mimeTypes) {
			handler.getMimeTypes().addMimeMapping(mimeType.getExtension(), mimeType.getName());
		}

		if (resourceBase != null) {
			handler.setResourceBase(resourceBase);
		}

		return handler;
	}
}
