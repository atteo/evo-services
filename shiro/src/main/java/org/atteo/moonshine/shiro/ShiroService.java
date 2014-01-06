/*
 * Copyright 2012 Atteo.
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
package org.atteo.moonshine.shiro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.TopLevelService;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * ShiroService service.
 *
 * <p>
 * Binds {@link SecurityManager}.
 * </p>
 */
@XmlRootElement(name = "shiro")
public class ShiroService extends TopLevelService {

	/**
	 * Enables Shiro AOP functionality.
	 *
	 * <p>
	 * With Shiro AOP you can use annotations for permission checking:
	 * {@link RequiresPermissions}, {@link RequiresUser}, {@link RequiresGuest}, etc.
	 * </p>
	 */
	@XmlElement
	@XmlDefaultValue("true")
	private Boolean aop;

	@XmlElementWrapper(name = "realms")
	@XmlElementRef
	private List<RealmService> realms = new ArrayList<>();

	/**
	 * URL prefix to filter through ShiroService.
	 */
	@XmlElement
	private String prefix = "/*";

	@Override
	public Iterable<? extends Service> getSubServices() {
		return realms;
	}

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				install(new ShiroModule() {
					@Override
					protected void configureShiro() {
						Multibinder<Realm> setBinder = Multibinder.newSetBinder(binder(), Realm.class);
						for (RealmService realm : realms) {
							if (realm.getId() == null) {
								setBinder.addBinding().to(Realm.class);
							} else {
								setBinder.addBinding().to(Key.get(Realm.class, Names.named(realm.getId())));
							}

						}

						try {
							// Guice will initialize manager with list of realms
							bind(WebSecurityManager.class).toConstructor(
									DefaultWebSecurityManager.class.getConstructor(Collection.class))
									.asEagerSingleton();
						} catch (NoSuchMethodException e) {
							addError(e);
						}
						expose(WebSecurityManager.class);
					}

					@Override
					protected void bindSessionManager(AnnotatedBindingBuilder<SessionManager> bind) {
						// make configurable
						bind.to(DefaultWebSessionManager.class).asEagerSingleton();
					}
				});
				FilterChainResolver filterChainResolver = new FilterChainResolver() {
					@Override
					public FilterChain getChain(ServletRequest request, ServletResponse response,
							FilterChain chain) {
						return null;
					}
				};
				bind(FilterChainResolver.class).toInstance(filterChainResolver);

				bind(GuiceShiroFilter.class).asEagerSingleton();

				install(ShiroWebModule.guiceFilterModule(prefix));
				if (aop) {
					install(new ShiroAopModule());
				}

				expose(SecurityManager.class);
				expose(WebSecurityManager.class);
			}
		};
	}

	@Inject
	private SecurityManager securityManager;

	@Override
	public void start() {
		SecurityUtils.setSecurityManager(securityManager);
	}

	@Override
	public void close() {
		SecurityUtils.setSecurityManager(null);

		ThreadContext.unbindSecurityManager();
		ThreadContext.unbindSubject();
	}
}
