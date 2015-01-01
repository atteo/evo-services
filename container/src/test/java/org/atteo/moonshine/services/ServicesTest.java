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

import java.util.List;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.util.Lists;
import org.atteo.moonshine.ConfigurationException;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;

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

	@Test
	public void shouldInjectServiceWhenPrivateModuleIsUsed() throws ConfigurationException {
		try (Services services = Services.Factory.builder()
				.configuration(new AbstractService() {
					@Inject
					private String helloWorld;

					@Override
					public Module configure() {
						return new PrivateModule() {
							@Override
							protected void configure() {
								bind(String.class).toInstance("Hello World");
							}
						};
					}
				})
				.build()) {
				}
	}

	@Test
	public void shouldInjectRequestInjection() throws ConfigurationException {
		class ProviderService extends AbstractService {
			@Override
			public String getId() {
				return "provider";
			}

			@Override
			public Module configure() {
				return new AbstractModule() {
					@Override
					protected void configure() {
						bind(String.class).toInstance("Hello World!");
					}
				};
			}
		}
		class Consumer {
			@Inject
			public String hello;
		}
		final Consumer consumer = new Consumer();

		class ConsumerService extends AbstractService {
			@ImportService
			public ProviderService service;

			@Override
			public Module configure() {
				return new AbstractModule() {
					@Override
					protected void configure() {
						requestInjection(consumer);
					}
				};
			}

		}
		try (Services services = Services.Factory.builder()
				.configuration(new AbstractService() {
					@Override
					public Iterable<? extends Service> getSubServices() {
						ProviderService providerService = new ProviderService();
						ConsumerService consumerService = new ConsumerService();
						consumerService.service = providerService;

						return Lists.newArrayList(providerService, consumerService);
					}
				}).build()) {

			assertThat(consumer.hello).isEqualTo("Hello World!");
		}
	}

	@Test
	public void shouldStartTwoInstancesOfTheSameServiceWithDifferentId() throws ConfigurationException {
		class Service extends AbstractService {
			private final String id;

			public Service(String id) {
				this.id = id;
			}

			@Override
			public String getId() {
				return id;
			}
		}

		try (Services services = Services.Factory.builder()
				.configuration(new AbstractService() {
					@Override
					public Iterable<? extends Service> getSubServices() {
						return Lists.newArrayList(new Service("a"), new Service("b"));
					}
				})
				.build()) {
			services.start();
		}
	}

	@Test
	public void shouldSupportTwoServicesWithTheSameId() throws ConfigurationException {
		class Service extends AbstractService {
		}

		try (Services services = Services.Factory.builder()
				.configuration(new AbstractService() {
					@Override
					public Iterable<? extends Service> getSubServices() {
						return Lists.newArrayList(new Service(), new Service());
					}
				})
				.build()) {
			services.start();
		}
	}

	@Test
	public void shouldBindListOfServices() throws ConfigurationException {
		// given
		try (Services services = Services.Factory.builder()
				.build()) {
			// when
			List<? extends ServiceInfo> servicesInfo = services.getGlobalInjector()
					.getInstance(Key.get(new TypeLiteral<List<? extends ServiceInfo>>() {}));

			// then
			assertThat(servicesInfo).isNotEmpty();
		}
	}
}
