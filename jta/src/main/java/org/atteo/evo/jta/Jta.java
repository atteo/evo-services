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
package org.atteo.evo.jta;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.services.TopLevelService;

import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;

/**
 * Generic JTA support service.
 * 
 * <p>
 * Provides support for &#064;{@link Transactional} annotation
 * and {@link Transaction} helper.
 * </p>
 * <p>
 * Requires JTA implementation to be present.
 * </p>
 */
@XmlRootElement(name = "jta")
public class Jta extends TopLevelService {
	/**
	 * Register {@link JtaFilter} which wraps web requests handling inside JTA transaction.
	 */
	@XmlElement
	private boolean registerWebFilter = false;

	@Override
	public Module configure() {
		return new ServletModule() {
			@Override
			protected void configureServlets() {
				requestStaticInjection(Transaction.class);
				TransactionalInterceptor interceptor = new TransactionalInterceptor();
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), interceptor);
				bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), interceptor);

				if (registerWebFilter) {
					filter("/*").through(JtaFilter.class);
				}
			}
		};
	}
}
