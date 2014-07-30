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
package org.atteo.moonshine.jetty.handlers;

import java.util.Collections;

import org.atteo.config.AbstractConfigurable;
import org.eclipse.jetty.server.Handler;


public abstract class HandlerConfig extends AbstractConfigurable {
	public abstract Handler getHandler();

	public Iterable<HandlerConfig> getSubHandlers() {
		return Collections.emptyList();
	}
}
