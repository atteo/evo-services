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


import java.io.IOException;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

public class JsonMessagesEndpoint extends Endpoint {
	private final HandlerDispatcher dispatcher;

	public JsonMessagesEndpoint(HandlerDispatcher handlers) {
		this.dispatcher = handlers;
	}

	@Override
	public void onOpen(final Session session, EndpointConfig config) {
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message) {
				try {
					String result = dispatcher.callOnMessage(message);
					session.getBasicRemote().sendText(result);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
	}
}
