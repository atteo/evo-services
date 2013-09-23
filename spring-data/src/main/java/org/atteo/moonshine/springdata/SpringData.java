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
package org.atteo.moonshine.springdata;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.moonshine.jpa.JpaService;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.TopLevelService;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.google.common.base.Strings;
import com.google.inject.Module;
import com.google.inject.PrivateModule;

/**
 * Creates and binds Spring Data JPA repositories.
 */
@XmlRootElement
public class SpringData extends TopLevelService {
	@XmlIDREF
	@ImportService
	private JpaService jpa;

	/**
	 * Build repositories only from the specified package or below.
	 */
	@XmlElement
	private String packagePrefix = "";

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(RepositoryFactorySupport.class).toProvider(new RepositoryFactoryProvider());

				if (Strings.isNullOrEmpty(packagePrefix)) {
					return;
				}
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

			private <T> void configureProvider(Class<T> klass) {
				if (klass.getAnnotation(NoRepositoryBean.class) != null) {
					return;
				}
				if (!klass.getCanonicalName().startsWith(packagePrefix + ".")) {
					return;
				}
				bind(klass).toProvider(new RepositoryProvider<>(klass));
				expose(klass);
			}
		};
	}
}
