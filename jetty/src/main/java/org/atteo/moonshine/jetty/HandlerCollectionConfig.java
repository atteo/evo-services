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
package org.atteo.moonshine.jetty;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerCollection;


/**
 * A collection of handlers.
 * <p>
 * The default implementations calls all handlers in list order,
 * regardless of the response status or exceptions. Derived implementation
 * may alter the order or the conditions of calling the contained handlers.
 * </p>
 */
@XmlRootElement(name = "handlerCollection")
public class HandlerCollectionConfig extends HandlerConfig {
	/**
	 * List of handlers.
	 */
	@XmlElementRef
	private HandlerConfig[] handlers = new HandlerConfig[0];

	protected HandlerCollection createCollection() {
		return new HandlerCollection();
	}

	@Override
	public Handler getHandler() {
		HandlerCollection handlerCollection = createCollection();
		for (HandlerConfig handler : handlers) {
			handlerCollection.addHandler(handler.getHandler());
		}
		return handlerCollection;
	}
}
