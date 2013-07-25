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
package org.atteo.moonshine.services;

import java.util.Collections;

import org.atteo.evo.classindex.IndexSubclasses;
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
 * <p>
 * {@link #configure()} is executed once before creating {@link Injector Guice injector}.
 * {@link #deconfigure()} is executed just before destroying the injector. {@link #start()}
 * and {@link #stop()} methods can be executed separately any time.
 * </p>
 */
@IndexSubclasses(storeJavadoc = true)
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
	 * Deconfigure this service.
	 */
	public void deconfigure() {
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

	/**
	 * Returns a list of sub-services this service contains.
	 *
	 * Although usually the list will come from unmarshalling the XML file,
	 * there is nothing which forbids the service from generating the list on the fly.
	 * @return list of services
	 */
	public Iterable<? extends Service> getSubServices() {
		return Collections.emptyList();
	}
}
