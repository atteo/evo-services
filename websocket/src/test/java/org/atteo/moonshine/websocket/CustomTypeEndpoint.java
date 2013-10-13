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

package org.atteo.moonshine.websocket;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

public class CustomTypeEndpoint extends Endpoint {
	@Override
	public void onOpen(Session session, final EndpointConfig config) {
		final RemoteEndpoint.Basic remote = session.getBasicRemote();
		session.addMessageHandler(new CustomMessageHandler(remote, config));
	}

	@Override
	public void onError(Session session, Throwable thr) {
		try {
			session.getBasicRemote().sendText("Exception: " + thr.toString());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}


	public static class CustomMessageHandler implements MessageHandler.Whole<CustomType> {

		private final RemoteEndpoint.Basic remote;
		private final EndpointConfig config;

		public CustomMessageHandler(RemoteEndpoint.Basic remote, EndpointConfig config) {
			this.remote = remote;
			this.config = config;
		}

		@Override
		public void onMessage(CustomType message) {
			try {
				remote.sendObject(new CustomType(config.getUserProperties().get("prefix") + message.getMessage()));
			} catch (IOException | EncodeException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
