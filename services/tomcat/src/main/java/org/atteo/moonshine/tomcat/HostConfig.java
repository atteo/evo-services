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
package org.atteo.moonshine.tomcat;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.atteo.config.Configurable;

/**
 * Virtual host configuration.
 */
public class HostConfig extends Configurable {
	/**
	 * Application base directory for virtual host.
	 */
	@XmlElement
	private String appBase = ".";

	/**
	 * Network name of this virtual host.
	 */
	@XmlElement
	private String name = "127.0.0.1";

	/**
	 * Contexts.
	 */
	@XmlElementWrapper(name = "contexts")
	@XmlElement(name = "context")
	private List<ContextConfig> contexts = new ArrayList<ContextConfig>() {
		{
			add(new ContextConfig());
		}
	};

	public String getAppBase() {
		return appBase;
	}

	public String getName() {
		return name;
	}

	public List<ContextConfig> getContexts() {
		return contexts;
	}
}
