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
package org.atteo.moonshine.jetty.handlers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.config.Configurable;
import org.eclipse.jetty.server.handler.ContextHandler;


/**
 * Jetty context handler.
 * <p>
 * This handler wraps a call to handle
 * by setting the context and servlet path, plus setting the context classloader.
 * </p>
 */
@XmlRootElement(name = "context")
public class ContextHandlerConfig extends Configurable {
	/**
	 * Set the context path. Only requests that have a matching path
	 * will be passed to the wrapped handler.
	 */
	@XmlElement
	private String contextPath = "/";

	/**
	 * Set the virtual hosts for the context. Only requests that have a matching
	 * host header or fully qualified URL will be passed to that context
	 * with a virtual host name. A context with no virtual host names or a null
	 * virtual host name is available to all requests that are not served
	 * by a context with a matching virtual host name.
	 */
	@XmlElement
	private String[] virtualHosts;

	@XmlElementRef(required = true)
	private HandlerConfig wrappedHandler;

	public ContextHandler getHandler() {
		ContextHandler contextHandler = new ContextHandler();
		contextHandler.setContextPath(contextPath);
		contextHandler.setHandler(wrappedHandler.getHandler());
		contextHandler.setVirtualHosts(virtualHosts);

		return contextHandler;
	}

	public HandlerConfig getWrappedHandlerConfig() {
		return wrappedHandler;
	}
}
