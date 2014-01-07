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
package org.atteo.moonshine;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.Configurable;
import org.atteo.moonshine.services.EmptyImplementation;
import org.atteo.moonshine.services.Service;

import com.google.inject.Module;

/**
 * Service which can be configured from XML configuration file.
 * <p>
 * Each service must define the name of the tag under which it can be referenced
 * in the configuration file using {@literal @}{@link XmlRootElement} annotation.
 * If you want your Service to be directly under the root tag in the configuration file it should
 * extend {@link TopLevelService}.
 * </p>
 */
public abstract class ConfigurableService extends Configurable implements Service {
	@EmptyImplementation
	@Override
	public Module configure() {
	    return null;
	}

	@EmptyImplementation
	@Override
	public void start() {
	}

	@EmptyImplementation
	@Override
	public void stop() {
	}

	@EmptyImplementation
	@Override
	public void close() {
	}

	@Nonnull
	@Override
	public Iterable<? extends Service> getSubServices() {
		return Collections.emptyList();
	}
}
