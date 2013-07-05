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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import com.google.inject.Module;

abstract public class HandlerWrapperConfig extends HandlerConfig {
	@XmlElementRef
	protected HandlerConfig wrappedHandler;

	abstract protected HandlerWrapper createHandler();

	@Override
	public Module configure() {
		if (wrappedHandler == null) {
			return null;
		}
		return wrappedHandler.configure();
	}

	@Override
	public Handler getHandler() {
		HandlerWrapper handler = createHandler();
		if (wrappedHandler != null) {
			handler.setHandler(wrappedHandler.getHandler());
		}
		return handler;
	}

}
