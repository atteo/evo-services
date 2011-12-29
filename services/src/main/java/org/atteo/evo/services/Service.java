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

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.servlet.ServletModule;

/**
 * Service.
 *
 * <p>
 * Services are created and started by {@link Services} from configuration file.
 * You should try to initialize all of the service logic in {@link Provider}s registered
 * in {@link Module} returned by {@link #configure()}. In this way the order of initialization
 * will be handled by {@link Guice} based on the dependencies between the services.
 * All of the registered {@link Binding}s should be usable even before execution
 * of {@link #start()} and {@link #stop()} methods.
 * </p>
 */
public abstract class Service extends Configurable {
	/**
	 * Build Guice {@link Module} with injection {@link Binding}s.
	 * Specifically {@link ServletModule}'s bindings are also supported.
	 * No fields will be injected in this object yet when executing this method.
	 * @return Guice module
	 */
	public Module configure() {
	    return null;
	}

	/**
	 * Start this service.
	 *
	 * When executing this method all fields will already be injected.
	 */
	public void start() {
	}

	/**
	 * Stop this service.
	 */
	public void stop() {
	}
}
