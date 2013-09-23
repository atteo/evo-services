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


import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.ConfigurationException;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Configures and starts Moonshine {@link Service services}.
 *
 * <p>
 * Each of the Services should extend {@link Service}. This interface defines
 * {@link Service#configure() configure()} method which allows to register Guice {@link Module},
 * {@link Service#start() start()} and {@link Service#stop() stop()} methods to execute some logic
 * upon start and stop of the application. If you want your Service to be directly
 * under the root directory in the configuration file it should extend {@link TopLevelService}.
 * Additionally each service must define the name of the tag under which it can be referenced
 * in the configuration file using {@literal @}{@link XmlRootElement} annotation.
 * </p>
 */
public interface Services extends AutoCloseable {

	public static interface Builder {
		/**
		 * Adds custom Guice module.
		 */
		Builder addModule(Module module);

		/**
		 * Sets services configuration.
		 */
		Builder configuration(Service config);

		/**
		 * Builds {@link Services} based on this builder parameters.
		 */
		Services build() throws ConfigurationException;
	}

	public static class Factory {
		public static Builder builder() {
			return new ServicesImplementation();
		}
	}

	/**
	 * Returns global injector.
	 * @return global injector
	 */
	Injector getGlobalInjector();

	/**
	 * Returns a mapping from {@link Service} to the list of bindings it registers.
	 *
	 * <p>
	 * This should be used only for debug purposes.
	 * </p>
	 * @return map from service to the list of bindings
	 */
	List<? extends ServiceInfo> getServiceElements();

	/**
	 * Starts all services.
	 */
	void start();

	/**
	 * Stops all services.
	 */
	void stop();

	/**
	 * Stops all services and destroys the injector.
	 */
	@Override
	void close();
}

