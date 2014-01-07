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

package org.atteo.moonshine.reflection;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ReflectionUtilsTest {
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnAncestors() {
		// given
		class A {
		}
		class B extends A {
		}

		// when
		Iterable<Class<? super B>> ancestors = ReflectionUtils.getAncestors(B.class);

		// then
		assertThat(ancestors).contains(A.class);
	}

	public void shouldReturnTrueWhenMethodOverriden() {
		// given
		class A {
			void method() {}
		}
		class B extends A {
			@Override
			void method() {}
		}

		// when
		boolean isOverriden = ReflectionUtils.isMethodOverriden(B.class, Object.class, "a");

		// then
		assertThat(isOverriden).isTrue();
	}

	public void shouldReturnFalseWhenMethodNotOverriden() {
		// given
		class A {
			void method() {}
		}
		class B extends A {
		}

		// when
		boolean isOverriden = ReflectionUtils.isMethodOverriden(B.class, Object.class, "a");

		// then
		assertThat(isOverriden).isFalse();
	}

	@Test
	public void shouldFindMethod() {
		// given
		class A {
			void methodFromA() {};
		}
		class B extends A {
			void methodFromB() {};
		}
		// when
		Method methodFromA = ReflectionUtils.findMethod(B.class, "methodFromA");
		Method methodFromB = ReflectionUtils.findMethod(B.class, "methodFromB");
		Method notExistingMethod = ReflectionUtils.findMethod(B.class, "notExistingMethod");

		// then
		// then
		assertThat(methodFromA.getDeclaringClass()).isEqualTo(A.class);
		assertThat(methodFromB.getDeclaringClass()).isEqualTo(B.class);
		assertThat(notExistingMethod).isNull();
	}
}
