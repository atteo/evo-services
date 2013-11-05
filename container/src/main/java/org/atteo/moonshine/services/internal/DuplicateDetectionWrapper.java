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
package org.atteo.moonshine.services.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.PrivateModule;
import com.google.inject.internal.ProviderMethodsModule;
import com.google.inject.spi.Elements;

/**
 * Allows to enhance the module with the logic to skip duplicate modules installation.
 */
public class DuplicateDetectionWrapper {
	private final Set<Module> modules = new HashSet<>();

	/**
	 * Wraps the module into another one which skips any modules which are already installed.
	 *
	 * <p>
	 * Note: does not work for modules rewritten using {@link Elements#getModule(Iterable)}.
	 * </p>
	 */
	public Module wrap(final Module module) {
		if (module instanceof PrivateModule) {
			return new PrivateModule() {
				@Override
				protected void configure() {
					if (modules.add(module)) {
						module.configure(createForwardingBinder(binder()));
						install(ProviderMethodsModule.forModule(module));
					}
				}
			};
		}
		return new Module() {
			@Override
			public void configure(Binder binder) {
				if (modules.add(module)) {
					module.configure(createForwardingBinder(binder));
					binder.install(ProviderMethodsModule.forModule(module));
				}
			}
		};
	}

	private Binder createForwardingBinder(final Binder binder) {
		if (binder instanceof PrivateBinder) {
			return (Binder) Proxy.newProxyInstance(DuplicateDetectionWrapper.class.getClassLoader(),
					new Class<?>[] { PrivateBinder.class }, new ForwardingBinderInvocationHandler(binder));
		}
		return (Binder) Proxy.newProxyInstance(DuplicateDetectionWrapper.class.getClassLoader(),
				new Class<?>[] { Binder.class }, new ForwardingBinderInvocationHandler(binder));
	}

	private class ForwardingBinderInvocationHandler implements InvocationHandler {

		private final Binder binder;

		public ForwardingBinderInvocationHandler(Binder binder) {
			this.binder = binder;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			switch (method.getName()) {
				case "install":
					Module module = (Module) args[0];
					binder.install(wrap(module));
					return null;
				case "withSource":
					Binder withSourceBinder = binder.withSource(args[0]);
					return createForwardingBinder(withSourceBinder);
				case "skipSources":
					Binder skipSourcesBinder = binder.skipSources((Class[]) args[0]);
					return createForwardingBinder(skipSourcesBinder);
				case "newPrivateBinder":
					PrivateBinder privateBinder = binder.newPrivateBinder();
					return createForwardingBinder(privateBinder);
			}
			return method.invoke(binder, args);
		}
	}
}
