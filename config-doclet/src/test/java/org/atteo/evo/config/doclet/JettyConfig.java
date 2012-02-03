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
package org.atteo.evo.config.doclet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;

/**
 * Wrapper around Jetty.
 * 
 * <p>
 * This element will execute Jetty instance on the specified port.
 * </p>
 */
@XmlRootElement
public class JettyConfig extends ConfigEntry {
	/**
	 * Port to listen on.
	 */
	@XmlAttribute(required = true)
	private int port = 8080;

	/**
	 * Port on which shutdown commands will be received.
	 * 
	 * This port must be specified on Windows where there is no other
	 * method of shutting down the application.
	 */
	@XmlElement
	@XmlDefaultValue("-1")
	int shutdownPort;
	
	@XmlElement
	private void setBase(String base) {
	}
}
