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

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlElement;

import org.atteo.moonshine.TopLevelService;

public abstract class Jaxrs extends TopLevelService {
	/**
	 * Automatically register in RESTEasy any class marked with &#064;
	 * {@link Path} or &#064;{@link Provider} annotations.
	 *
	 * Instances of auto-registered resources are created inside the RESTEasy service so they
	 * can't depend on any bindings from outside of it.
	 *
	 * Either all resources have to be discovered or all have to be added manually.
	 */
	@XmlElement
	protected boolean discoverResources = false;
}
