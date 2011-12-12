/*
 * Copyright 2011 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.evo.jaxb;

import java.lang.reflect.Field;

import javax.annotation.Nullable;
import javax.xml.bind.Binder;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Iterates over JAXB bindings.
 */
// TODO: add iteration over attributes
public class JaxbBindings {
	public interface Runnable {
		/**
		 * Executed for each found element/object JAXB binding.
		 * @param element DOM element
		 * @param object object which element was unmarshalled to.
		 * @param field field which this element was unmarshalled to, can be null
		 */
		void run(Element element, Object object, @Nullable Field field);
	}

	private Binder binder;
	private Runnable runnable;

	private JaxbBindings(Binder binder, Runnable runnable) {
		this.binder = binder;
		this.runnable = runnable;
	}

	/**
	 * Iterates over pairs consisting of {@link Element} and an object to which it was unmarshalled.
	 * @param root root XML element
	 * @param binder binder with the binding info
	 * @param runnable runnable to run for each {@link Element}/{@link Object} pair.
	 */
	public static void iterate(Element root, Binder binder, Runnable runnable) {
		Object object = binder.getJAXBNode(root);
		JaxbBindings bindRecurse = new JaxbBindings(binder, runnable);
		try {
			bindRecurse.recurse(root, object, null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void recurse(Element element, Object object, @Nullable Field field)
			throws IllegalAccessException {
		runnable.run(element, object, field);

		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (!(node instanceof Element)) {
				continue;
			}
			Element e = (Element) node;
			Object child = binder.getJAXBNode(node);
			Field f;
			if (child != null) {
				f = findFieldByValue(object, child);
			} else {
				f = findFieldByXmlElementWrapper(object, e.getTagName());
				if (f == null) {
					continue;
				}
				child = f.get(object);
			}
			recurse(e, child, f);
		}
	}

	private static Field findFieldByValue(Object object, Object child)
			throws IllegalAccessException {
		Class<?> klass = object.getClass();
		while (klass != Object.class) {
			for (Field field : klass.getDeclaredFields()) {
				field.setAccessible(true);
				if (field.get(object) == child) {
					return field;
				}
			}
			klass = klass.getSuperclass();
		}
		return null;
	}

	private static Field findFieldByXmlElementWrapper(Object object, String name)
			throws IllegalAccessException {
		Class<?> klass = object.getClass();
		while (klass != Object.class) {
			for (Field field : klass.getDeclaredFields()) {
				field.setAccessible(true);
				XmlElementWrapper annotation = field.getAnnotation(XmlElementWrapper.class);
				// TODO: should also check namespace here
				if (annotation != null && name.equals(annotation.name())) {
					return field;
				}
			}
			klass = klass.getSuperclass();
		}
		return null;
	}
}
