/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.services;

import org.atteo.evo.config.Configurable;

import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

/**
 * Basic configurable component which can be {@link #configure() configured}, {@link #start() started}
 * and {@link #stop() stopped}.
 *
 * <p>
 * Services are instantiated by {@link Services} according to the provided configuration file.
 * Most of the logic which configures the Service should be put in {@link #configure()} method,
 * so it can be lazily initialized by {@link Guice} which knows about dependencies between services.
 * </p>
 */
public abstract class Service extends Configurable {
	/**
	 * Returns {@link Module} which specifies this service dependencies and what it provides
	 * to other services.
	 *
	 * <p>
	 * Additionally {@link ServletModule} implementation can also be returned
	 * which allows to register servlets and filters (you need appropriate service enabled which uses this info,
	 * like Jetty).
	 * </p>
	 *
	 * @return Guice module
	 */
	public Module configure() {
	    return null;
	}

	/**
	 * Optionally starts some logic which is not started automatically by Guice.
	 *
	 * <p>
	 * All class fields marked with {@link Inject} will be already injected before execution of this method.
	 * </p>
	 */
	public void start() {
	}

	/**
	 * Stops this service.
	 */
	public void stop() {
	}
}
