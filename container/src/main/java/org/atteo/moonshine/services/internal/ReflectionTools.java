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

import javax.inject.Singleton;


public class ReflectionTools {
	/**
	 * Checks whether class is marked as singleton.
	 */
	public static boolean isSingleton(Class<?> klass) {
		return klass.isAnnotationPresent(Singleton.class)
				|| klass.isAnnotationPresent(com.google.inject.Singleton.class);
	}

	/**
	 * Checks if given method is overridden.
	 * @param <T> klass type
	 * @param klass class to check method on, should be child of parentClass
	 * @param parentClass class which marks stop of the search
	 * @param methodName name of the method to search
	 * @return true, if method is overridden in klass, or any of its super classes up to (exclusive) parent class
	 */
	public static <T> boolean isMethodOverriden(Class<? extends T> klass, Class<T> parentClass, String methodName) {
		Class<?> superClass = klass;
		while (superClass != parentClass) {
			try {
				superClass.getDeclaredMethod(methodName);
			} catch (NoSuchMethodException e) {
				superClass = superClass.getSuperclass();
				continue;
			}
			return true;
		}
		return false;
	}
}
