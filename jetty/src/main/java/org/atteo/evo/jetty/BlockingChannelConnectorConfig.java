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
package org.atteo.evo.jetty;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;

/**
 * Blocking NIO connector.
 * This connector uses efficient NIO buffers with a traditional blocking thread model.
 * Direct NIO buffers are used and a thread is allocated per connections.
 * This connector is best used when there are a few very active connections.
 * 
 * @see BlockingChannelConnector
 */
@XmlRootElement(name = "blockingchannel")
public class BlockingChannelConnectorConfig extends AbstractConnectorConfig {
	@Override
	public Connector getConnector() {
		Connector connector = new BlockingChannelConnector();
		configure(connector);
		return connector;
	}
}
