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
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import javax.inject.Inject;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.atteo.moonshine.webserver.WebServerAddress;
import org.junit.Test;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <servlet-container/>"
		+ "    <jetty>"
		+ "        <connectors>"
		+ "            <serverconnector>"
		+ "                <provideAddress>true</provideAddress>"
		+ "            </serverconnector>"
		+ "        </connectors>"
		+ "    </jetty>"
		+ "    <jetty-websocket-container/>"
		+ "    <json-messages/>"
		+ "    <test-handlers/>"
		+ "</config>")
public class JsonMessagesServiceTest extends MoonshineTest {
	@Inject
	private WebServerAddress webServerAddress;

	interface Sender {
		void sendRequest(RequestMessage message);
		void sendPing(PingMessage ping);
	}

	@Test
	public void shouldTransferMessage() throws URISyntaxException, DeploymentException, IOException,
			InterruptedException {
		final TransferQueue<String> queue = new LinkedTransferQueue<>();
		class Handler {
			@OnMessage
			public void onMessage(ResponseMessage response) {
				try {
					queue.transfer(response.getMessage());
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		JsonMessages.Factory factory = JsonMessages.factory();
		factory.addHandler(new Handler());
		SenderProvider<Sender> senderProvider = factory.addSender(Sender.class);

		String path = "/json-messages";
		URI uri =  new URI(webServerAddress.getUrl().replace("http://", "ws://") + path);
		Session session = factory.connect(uri);

		Sender sender = senderProvider.get(session);
		sender.sendRequest(new RequestMessage("Hello World"));

		// when
		String response = queue.take();

		// then
		assertThat(response).isEqualTo("Hello World");
	}

	@Test
	public void shouldRespondToPing() throws URISyntaxException, DeploymentException, IOException,
			InterruptedException {
		final TransferQueue<JsonMessage> queue = new LinkedTransferQueue<>();
		class Handler {
			@OnMessage
			public void onMessage(ResponseMessage response) {
				try {
					queue.transfer(response);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}

			@OnMessage
			public void onPong(PongMessage pong) {
				try {
					queue.transfer(pong);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		JsonMessages.Factory factory = JsonMessages.factory();
		factory.addHandler(new Handler());
		SenderProvider<Sender> senderProvider = factory.addSender(Sender.class);

		String path = "/json-messages";
		URI uri =  new URI(webServerAddress.getUrl().replace("http://", "ws://") + path);
		Session session = factory.connect(uri);

		Sender sender = senderProvider.get(session);
		sender.sendPing(new PingMessage());

		// when
		JsonMessage response = queue.take();

		// then
		assertThat(response).isInstanceOf(PongMessage.class);
	}
}
