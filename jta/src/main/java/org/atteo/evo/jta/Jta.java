package org.atteo.evo.jta;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.evo.services.TopLevelService;

import com.google.inject.AbstractModule;
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
