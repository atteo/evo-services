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
package org.atteo.moonshine.services;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.atteo.evo.classindex.IndexSubclasses;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

/**
 * Fundamental Moonshine component encapsulating some functionality.
 * <p>
 * Service can {@link #configure() register bindings}, can be repeatedly {@link #start() started}
 * and {@link #stop() stopped} and finally {@link #close() closed}.
 * </p>
 * <p>
 * Services are instantiated by {@link Services} according to the provided configuration.
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
public interface Service extends AutoCloseable {
	String getId();

	/**
	 * Returns {@link Module} which specifies this service dependencies and what it provides
	 * to other services.
	 *
	 * @return Guice module
	 */
	Module configure();

	/**
	 * Starts this service.
	 *
	 * <p>
	 * All fields marked with {@link Inject} will be injected before execution of this method.
	 * </p>
	 * <p>
	 * This method should be used to start functionalities which are used outside this application.
	 * Functionality that is exported to other services should be started in {@link #configure()} method
	 * or from {@link Provider} to correctly handle dependencies between services.
	 * </p>
	 */
	void start();

	/**
	 * Stops this service.
	 *
	 * <p>
	 * Service can still be started later using {@link #start()} method.
	 * </p>
	 */
	void stop();

	/**
	 * Closes this services, it won't be started again.
	 */
	@Override
	void close();

	/**
	 * Returns a list of direct sub-services this service contains.
	 *
	 * <p>
	 * Although usually the list will come from unmarshalling the XML file,
	 * there is nothing which forbids the service from generating the list on the fly.
	 * </p>
	 * <p>
	 * Only direct sub-services should be returned. Moonshine will on its own ask
	 * returned sub-services for their sub-services.
	 * </p>
	 * @return list of services
	 */
	@Nonnull
	Iterable<? extends Service> getSubServices();
}
