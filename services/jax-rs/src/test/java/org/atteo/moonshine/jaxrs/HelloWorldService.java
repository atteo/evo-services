/*
 * Copyright 2014 Atteo.
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

package org.atteo.moonshine.jaxrs;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

@XmlRootElement(name = "hello-world")
public class HelloWorldService extends TopLevelService {
	@ImportService
	private Jaxrs jaxrs;

	@XmlAttribute
	private String message = "Hello World";

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(String.class).toInstance(message);
				bind(HelloWorldResource2.class);
				jaxrs.registerResource(HelloWorldResource2.class, getProvider(HelloWorldResource2.class));
			}
		};
	}

}
