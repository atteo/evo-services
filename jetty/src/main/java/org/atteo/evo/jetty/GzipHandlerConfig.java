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
package org.atteo.evo.jetty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.handler.GzipHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;

/**
 * GZIP Handler This handler will gzip the content of a response if:
 * <ul>
 * <li>The filter is mapped to a matching path</li>
 * <li>The response status code is >=200 and <300
 * <li>The content length is unknown or more than the <code>minGzipSize</code> initParameter or the minGzipSize is 0(default)</li>
 * <li>The content-type is in the comma separated list of mimeTypes set in the <code>mimeTypes</code> initParameter or if no mimeTypes are defined the
 * content-type is not "application/gzip"</li>
 * <li>No content-encoding is specified by the resource</li>
 * </ul>
 *
 * @see GzipHandler
 */
@XmlRootElement(name = "gzip")
public class GzipHandlerConfig extends HandlerWrapperConfig {
	/**
	 * Minimum content length triggering compression.
	 */
	@XmlElement
	private int minGzipSize = 0;

	@Override
	protected HandlerWrapper createHandler() {
		GzipHandler handler = new GzipHandler();
		handler.setMinGzipSize(minGzipSize);
		return handler;
	}
}
