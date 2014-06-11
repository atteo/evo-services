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

package org.atteo.moonshine.websocket.jsonmessages;

import javax.inject.Provider;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.websocket.WebSocketContainerService;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

/**
 * JSON WebSocket messages dispatcher.
 */
@XmlRootElement(name = "json-messages")
public class JsonMessagesService extends TopLevelService {
	@ImportService
	@XmlIDREF
	@XmlElement
	private WebSocketContainerService webSocketContainer;

	@XmlElement
	private String pattern = "/json-messages";

	private final HandlerDispatcher dispatcher = new HandlerDispatcher();

	public <T> void addHandler(Class<T> klass, Provider<? extends T> handler) {
		dispatcher.addHandler(klass, handler);
	}

	public <T> SenderProvider<T> addSender(Class<T> klass) {
		return dispatcher.addSender(klass);
	}

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(JsonMessagesEndpoint.class).toInstance(new JsonMessagesEndpoint(dispatcher));

				webSocketContainer.addEndpoint(JsonMessagesEndpoint.class)
						.provider(getProvider(JsonMessagesEndpoint.class))
						.pattern(pattern);
			}
		};
	}
}
