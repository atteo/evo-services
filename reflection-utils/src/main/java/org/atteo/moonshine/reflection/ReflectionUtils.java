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
import java.util.Deque;
import java.util.LinkedList;


public class ReflectionUtils {
	private ReflectionUtils() {
	}

	/**
	 * Return all ancestors of a given class.
	 *
	 * <p>
	 * Implemented interfaces are not returned.
	 * </p>
	 * <p>
	 * The classes are returned in order, supertypes are returned before subtypes.
	 * Object itself is not included.
	 * </p>
	 */
	public static <T> Iterable<Class<? super T>> getAncestors(Class<T> klass) {
		Deque<Class<? super T>> ancestors = new LinkedList<>();
		for (Class<?> ancestor = klass; ancestor != Object.class; ancestor = ancestor.getSuperclass()) {
			@SuppressWarnings("unchecked")
			final Class<? super T> acestorCasted = (Class<? super T>) ancestor;
			ancestors.addFirst(acestorCasted);
		}
		return ancestors;
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

	/**
	 * Finds method with given name in given class or in its nearest superclass.
	 * @param <T> klass type
	 * @param klass class to search method on
	 * @param methodName method name to search
	 * @return found method or null, if not found
	 */
	public static <T> Method findMethod(Class<? extends T> klass, String methodName) {
		Class<?> superClass = klass;
		while (superClass != Object.class) {
			try {
				return superClass.getDeclaredMethod(methodName);
			} catch (NoSuchMethodException e) {
			}
			superClass = superClass.getSuperclass();
		}
		return null;
	}
}
