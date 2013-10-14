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
import java.net.URI;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

/**
 * Client side JSON messages support.
 */
public class JsonMessages {
	public interface Factory {
		<T> Factory addHandler(T handler);
		<T> SenderProvider<T> addSender(Class<T> senderClass);
		Session connect(URI uri);
	}

	public static Factory factory() {
		final HandlerDispatcher dispatcher = new HandlerDispatcher();
		return new Factory() {
			@Override
			public <T> Factory addHandler(T handler) {
				dispatcher.addHandler(handler);
				return this;
			}

			@Override
			public <T> SenderProvider<T> addSender(Class<T> senderClass) {
				return dispatcher.addSender(senderClass);
			}

			@Override
			public Session connect(URI uri) {
				ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

				try {
					return ContainerProvider.getWebSocketContainer().connectToServer(
							new JsonMessagesEndpoint(dispatcher), config, uri);
				} catch (DeploymentException | IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		};
	}

}
