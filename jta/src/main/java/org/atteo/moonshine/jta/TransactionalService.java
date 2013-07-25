/*
 * Copyright 2011 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.jta;

import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.services.ImportBindings;
import org.atteo.moonshine.services.TopLevelService;

import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;

/**
 * Adds support for {@link Transactional} annotation.
 * <p>
 * Provides support for &#064;{@link Transactional} annotation,
 * {@link Transaction} helper and optionally registers web filter which wraps servlet request
 * in separate transaction.
 * </p>
 */
@XmlRootElement(name = "transactional")
@Singleton
public class TransactionalService extends TopLevelService {
	@XmlIDREF
	@ImportBindings
	private JtaService jtaService;

	/**
	 * Register {@link JtaFilter} which wraps web requests handling inside JTA transaction.
	 */
	@XmlElement
	private boolean registerWebFilter = false;

	/**
	 * Filter pattern matching urls for which {@link JtaFilter} will be registered.
	 */
	@XmlElement
	@XmlDefaultValue("/*")
	private String filterPattern;

	@Override
	public Module configure() {
		return new ServletModule() {
			@Override
			protected void configureServlets() {
				bind(JtaFilter.class);

				requestStaticInjection(Transaction.class);
				TransactionalInterceptor interceptor = new TransactionalInterceptor();
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), interceptor);
				bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), interceptor);

				if (registerWebFilter) {
					filter(filterPattern).through(JtaFilter.class);
				}
			}
		};
	}
}
