/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.injection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class InjectMembersModule implements Module, TypeListener {
	@Override
	public void configure(Binder binder) {
		binder.bindListener(Matchers.any(), this);
	}

	@Override
	public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
		Class<? super I> klass = type.getRawType();

		List<Field> fields = new ArrayList<Field>();

		while (klass != Object.class) {
			for (Field field : klass.getDeclaredFields()) {
				if (field.getAnnotation(InjectMembers.class) != null) {
					fields.add(field);
				}
			}

			klass = klass.getSuperclass();
		}

		if (fields.isEmpty()) {
			return;
		}
		Provider<Injector> injector = encounter.getProvider(Injector.class);

		encounter.register(generateListener(fields, injector));
	}

	private <T> InjectionListener<T> generateListener(final Collection<Field> fields,
			final Provider<Injector> injectorProvider) {
		return new InjectionListener<T>() {
			@Override
			public void afterInjection(T instance) {
				for (Field field : fields) {
					boolean wasAccessible = field.isAccessible();
					field.setAccessible(true);
					Injector injector = injectorProvider.get();

					try {
						Object object = field.get(instance);
						if (object == null) {
							continue;
						}

						if (object instanceof Collection) {
							for (Object o : (Collection<?>) object) {
								injector.injectMembers(o);
							}
						} else {
							injector.injectMembers(object);
						}
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					field.setAccessible(wasAccessible);
				}
			}
		};
	}
}
