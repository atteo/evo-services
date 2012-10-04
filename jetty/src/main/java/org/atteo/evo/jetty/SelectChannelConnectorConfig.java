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
import org.eclipse.jetty.server.nio.SelectChannelConnector;

/**
 * This connector uses efficient NIO buffers with a non blocking threading model.
 * Direct NIO buffers are used and threads are only allocated to connections with requests.
 * Synchronization is used to simulate blocking for the servlet API, and any unflushed content
 * at the end of request handling is written asynchronously.
 *
 * This connector is best used when there are a many connections that have idle periods.
 *
 * @see SelectChannelConnector
 */
@XmlRootElement(name = "selectchannel")
public class SelectChannelConnectorConfig extends AbstractConnectorConfig {
	@Override
	public Connector createConnector() {
		return new SelectChannelConnector();
	}
}
