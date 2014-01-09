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

package org.atteo.moonshine.services;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.util.Lists;
import org.atteo.moonshine.ConfigurationException;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.Module;

public class ServicesTest {
	@Test
	public void shouldFireListeners() throws ConfigurationException {
		LifeCycleListener listener = Mockito.mock(LifeCycleListener.class);
		try (Services services = Services.Factory.builder().registerListener(listener).build()) {
			Mockito.verify(listener).configured(services.getGlobalInjector());
			services.start();
			Mockito.verify(listener).started();
		}
		Mockito.verify(listener).stopping();
		Mockito.verify(listener).closing();
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFireStopListenerOnError() throws ConfigurationException {
		LifeCycleListener listener = Mockito.mock(LifeCycleListener.class);
		try (Services services = Services.Factory.builder()
				.registerListener(listener)
				.configuration(new AbstractService() {
					@Override
					public void start() {
						throw new IllegalStateException("expected");
					}
				})
				.build()) {
			Mockito.verify(listener).configured(services.getGlobalInjector());
			services.start();
			Mockito.verify(listener, Mockito.times(0)).started();
		} finally {
			Mockito.verify(listener).stopping();
			Mockito.verify(listener).closing();
		}
	}

	@Test
	public void shouldStartServicesInDependencyOrder() throws ConfigurationException {
		class ServiceA extends AbstractService {
			boolean started = false;
			@Override
			public void start() {
				started = true;
			}

			@Override
			public void stop() {
				started = false;
			}
		}
		class ServiceB extends AbstractService {
			@ImportService
			private ServiceA serviceA;

			@Override
			public void start() {
				assertThat(serviceA.started).isTrue();
			}

			@Override
			public void stop() {
				assertThat(serviceA.started).isTrue();
			}
		}

		try (Services services = Services.Factory.builder()
				.configuration(new AbstractService() {
					@Override
					public Iterable<? extends Service> getSubServices() {
						return Lists.newArrayList(new ServiceB(), new ServiceA());
					}
				})
				.build()) {
			services.start();
		}
	}

	@Test
	public void shouldConfigureCloseInDependencyOrder() throws ConfigurationException {
		class ServiceA extends AbstractService {
			boolean configured = false;
			@Override
			public Module configure() {
				configured = true;
				return null;
			}

			@Override
			public void close() {
				configured = false;
			}
		}
		class ServiceB extends AbstractService {
			@ImportService
			private ServiceA serviceA;

			@Override
			public Module configure() {
				assertThat(serviceA.configured).isTrue();
				return null;
			}

			@Override
			public void close() {
				assertThat(serviceA.configured).isTrue();
			}
		}

		try (Services services = Services.Factory.builder()
				.configuration(new AbstractService() {
					@Override
					public Iterable<? extends Service> getSubServices() {
						return Lists.newArrayList(new ServiceB(), new ServiceA());
					}
				})
				.build()) {
			services.start();
		}
	}
}
