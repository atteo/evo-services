package org.atteo.evo.jta;

import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.services.TopLevelService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

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
	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				requestStaticInjection(Transaction.class);
				TransactionalInterceptor interceptor = new TransactionalInterceptor();
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), interceptor);
				bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), interceptor);
			}
		};
	}
}
