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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import javax.inject.Inject;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.atteo.moonshine.webserver.WebServerAddress;
import org.junit.Test;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <servlet-container/>"
		+ "    <test-endpoints/>"
		+ "</config>")
public abstract class WebSocketContainerServiceTest extends MoonshineTest {
	@Inject
	private WebServerAddress webServerAddress;

	private final TransferQueue<String> queue = new LinkedTransferQueue<>();

	private void sendMessage(String path, final String message) throws URISyntaxException, DeploymentException, IOException {
		URI uri =  new URI(webServerAddress.getUrl().replace("http://", "ws://") + path);
		ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

		ContainerProvider.getWebSocketContainer().connectToServer(new Endpoint() {
			@Override
			public void onOpen(Session session, EndpointConfig config) {
				try {
					session.addMessageHandler(new MessageHandler.Whole<String>() {
						@Override
						public void onMessage(String message) {
							try {
								queue.transfer(message);
							} catch (InterruptedException ex) {
								throw new RuntimeException(ex);
							}

						}
					});
					session.getBasicRemote().sendText(message);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}, config, uri);
	}

	@Test
	public void shouldReceiveReplyUsingWebsocket() throws URISyntaxException, DeploymentException, IOException,
			InterruptedException {
		// given
		sendMessage("/echo", "Hello World");

		// when
		String message = queue.take();

		// then
		assertThat(message).isEqualTo("Hello World");
	}

	@Test
	public void shouldSupportEncoders() throws URISyntaxException, DeploymentException, IOException,
			InterruptedException {
		// given
		sendMessage("/custom", "request");

		// when
		String message = queue.take();

		// then
		assertThat(message).isEqualTo("request was: request");
	}
}
