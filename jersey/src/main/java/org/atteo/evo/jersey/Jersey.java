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
package org.atteo.evo.jersey;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.services.TopLevelService;

import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

@XmlRootElement(name = "jersey")
public class Jersey extends TopLevelService {
	/**
	 * Path under which Jersey should be registered.
	 */
	@XmlElement
	private String path = "/*";

	/**
	 * Automatically register in Jersey any classes marked with
	 * &#064;{@link Path} or &#064;{@link Provider} annotations.
	 */
	@XmlElement
	private boolean discoverResources = true;

	@Override
	public Module configure() {
		return new ServletModule() {
			@Override
			protected void configureServlets() {
				Map<String, String> params = new HashMap<String, String>();
				params.put(ServletContainer.FEATURE_FILTER_FORWARD_ON_404, "true");
				filter(path).through(GuiceContainer.class, params);
				bind(JAXBContextResolver.class);

				if (discoverResources) {
					for (Class<?> klass : ClassIndex.getAnnotated(Path.class)) {
						bind(klass);
					}
					for (Class<?> klass : ClassIndex.getAnnotated(Provider.class)) {
						bind(klass);
					}
				}
			}
		};
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}
}
