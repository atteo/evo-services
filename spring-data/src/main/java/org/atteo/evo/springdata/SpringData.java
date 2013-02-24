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
package org.atteo.evo.springdata;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.services.TopLevelService;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;

/**
 * Sprint Data integration.
 */
@XmlRootElement(name = "spring-data")
public class SpringData extends TopLevelService {
	private static class RepositoryFactoryProvider implements Provider<RepositoryFactorySupport> {
		@Inject
		private EntityManager manager;

		@Override
		public RepositoryFactorySupport get() {
			return new JpaRepositoryFactory(manager);
		}
	}

	private static class RepositoryProvider<T> implements Provider<T> {
		@Inject
		private RepositoryFactorySupport factory;

		private Class<T> klass;

		public RepositoryProvider(Class<T> klass) {
			this.klass = klass;
		}

		@Override
		public T get() {
			return factory.getRepository(klass);
		}
	}

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(RepositoryFactorySupport.class).toProvider(new RepositoryFactoryProvider());
				Set<Class<?>> classes = new HashSet<>();
				for (Class<? extends Repository> klass : ClassIndex.getSubclasses(Repository.class)) {
					configureProvider(klass);
					classes.add(klass);
				}
				for (Class<?> klass : ClassIndex.getAnnotated(RepositoryDefinition.class)) {
					if (classes.contains(klass)) {
						continue;
					}
					configureProvider(klass);
				}
			}

			private void configureProvider(Class<?> klass) {
				if (klass.getAnnotation(NoRepositoryBean.class) != null) {
					return;
				}
				bind(klass).toProvider(new RepositoryProvider(klass));
			}
		};
	}
}
