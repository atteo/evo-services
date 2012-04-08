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
package org.atteo.evo.shiro;

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
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.evo.services.TopLevelService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.binder.AnnotatedBindingBuilder;

@XmlRootElement
public class Shiro extends TopLevelService {

	/**
	 * Enable Shiro AOP functionality.
	 * 
	 * <p>
	 * Shiro AOP support enables you to use annotations for permission checking.
	 * Supported annotations include:
	 * {@link RequiresPermissions}, {@link RequiresUser}, {@link RequiresGuest}, etc.
	 * </p>
	 */
	@XmlElement
	@XmlDefaultValue("true")
	private Boolean aop;

	@XmlElementWrapper(name = "realms")
	@XmlElementRef
	private List<ShiroRealm> realms;
	
	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				install(new ShiroModule() {
					@Override
					protected void configureShiro() {
						if (realms != null) {
							for (ShiroRealm realm : realms) {
								this.install(realm.configure());
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

				install(ShiroWebModule.guiceFilterModule());
				if (aop) {
					install(new ShiroAopModule());
				}
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
	public void stop() {
		SecurityUtils.setSecurityManager(null);
	}
}
