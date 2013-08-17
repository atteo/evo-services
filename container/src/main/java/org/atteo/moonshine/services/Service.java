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

import javax.inject.Inject;
import javax.inject.Provider;

import org.atteo.evo.classindex.IndexSubclasses;
import org.atteo.evo.config.Configurable;

import com.google.inject.Guice;
import com.google.inject.Injector;
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
 * {@link #configure()} is executed only once before creating {@link Injector Guice injector} and can be used
 * to configure it. {@link #close()} is also executed only once, just before destroying the injector.
 * {@link #start()} and {@link #stop()} methods should start and stop any functionality which this service
 * provides to the outside world.
 * </p>
 */
@IndexSubclasses(storeJavadoc = true)
public abstract class Service extends Configurable implements AutoCloseable {
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
	 * Closes this services, it won't be started again.
	 */
	@Override
	public void close() {
	}

	/**
	 * Starts this service.
	 *
	 * <p>
	 * All fields marked with {@link Inject} will be injected before execution of this method.
	 * </p>
	 * <p>
	 * Every functionality that is exported to other services should be started in {@link #configure()} method
	 * or in registered {@link Provider}. Here you can start functionalities which are used outside this application.
	 * </p>
	 */
	public void start() {
	}

	/**
	 * Stops this service.
	 *
	 * <p>
	 * Service can still be started later using {@link #start()} method.
	 * </p>
	 */
	public void stop() {
	}

	/**
	 * Returns a list of sub-services this service contains.
	 *
	 * <p>
	 * Although usually the list will come from unmarshalling the XML file,
	 * there is nothing which forbids the service from generating the list on the fly.
	 * </p>
	 * @return list of services
	 */
	public Iterable<? extends Service> getSubServices() {
		return Collections.emptyList();
	}
}
