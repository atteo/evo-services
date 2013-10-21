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
import java.net.URISyntaxException;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import org.atteo.moonshine.webserver.WebServerAddress;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Client side JSON messages support.
 */
public class JsonMessages {
	public interface Factory {
		/**
		 * Adds handler.
		 * <p>
		 * Handler is an object with methods annotated with {@link OnMessage} annotation.
		 * Each such method must take exactly one argument whose type should implement
		 * {@link JsonMessage} interface and be annotated with {@link JsonTypeName}.
		 * Such method will be called when message of matching type will be received.
		 * Optionally handler method can return {@link JsonMessage} object which will be then
		 * sent back to the message sender.
		 * </p>
		 */
		<T> Factory addHandler(T handler);
		/**
		 * Adds message sender.
		 * <p>
		 * Message sender allows you to send messages through WebSocket. The approach here is similiar
		 * to that of Spring Data. You provide an interface with sender methods. Each such method
		 * should take exactly one argument whose type should implement {@link JsonMessage} interface
		 * and be annotated with {@link JsonTypeName}. The returned object {@link SenderProvider} allows
		 * you to retrieve implementation of the interface for given {@link Session}.
		 * You can then call sender methods to send given message.
		 * </p>
		 */
		<T> SenderProvider<T> addSender(Class<T> senderClass);
		/**
		 * Connect to the specified uri.
		 * <p>
		 * After the connection is established any registered handlers can be called when matching
		 * message will be received through this Web Socket.
		 * </p>
		 */
		Session connect(URI uri);
		/**
		 * Connect to the specified web server.
		 * <p>
		 * This is helper method mainly for use in tests which calls
		 * {@link #connect(URI)} with the address derived from given {@link WebServerAddress web server address}.
		 * </p>
		 */
		Session connect(WebServerAddress webServerAddress) throws URISyntaxException;
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

			@Override
			public Session connect(WebServerAddress webServerAddress) throws URISyntaxException {
				String path = "/json-messages";
				URI uri =  new URI(webServerAddress.getUrl().replace("http://", "ws://") + path);
				return connect(uri);
			}
		};
	}

}
