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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;
import javax.ws.rs.Path;
import javax.xml.bind.annotation.XmlElement;

import org.atteo.classindex.ClassIndex;
import org.atteo.moonshine.TopLevelService;

import com.google.inject.Binder;

public abstract class Jaxrs extends TopLevelService {
	/**
	 * Automatically register in JAX-RS any class marked with &#064;
	 * {@link Path} or &#064;{@link javax.ws.rs.ext.Provider} annotations.
	 *
	 * Instances of auto-registered resources are created inside the RESTEasy service so they
	 * can't depend on any bindings from outside of it.
	 *
	 * Either all resources have to be discovered or all have to be added manually.
	 */
	@XmlElement
	private boolean discoverResources = false;

	private final List<JaxrsResource<?>> resources = new ArrayList<>();

	/**
	 * Adds new resource.
	 * @param <T> resource class
	 * @param klass resource class
	 * @param provider resource provider
	 */
	public <T> void addResource(Class<T> klass, Provider<T> provider) {
		resources.add(new JaxrsResource<>(klass, provider));
	}

	private <T> void addResource(Class<T> annotated, Binder binder) {
		addResource(annotated, binder.getProvider(annotated));
	}

	/**
	 * Registers discovered resources.
	 */
	protected void registerResources(Binder binder) {
		if (discoverResources) {
			for (Class<?> annotated : ClassIndex.getAnnotated(Path.class)) {
				binder.bind(annotated);
				addResource(annotated, binder);
			}
		}
	}

	protected List<JaxrsResource<?>> getResources() {
		return resources;
	}

	protected static class JaxrsResource<T> {
		private final Class<T> klass;
		private final Provider<T> provider;

		public JaxrsResource(Class<T> klass, Provider<T> provider) {
			this.klass = klass;
			this.provider = provider;
		}

		public Class<T> getKlass() {
			return klass;
		}

		public Provider<T> getProvider() {
			return provider;
		}
	}
}
