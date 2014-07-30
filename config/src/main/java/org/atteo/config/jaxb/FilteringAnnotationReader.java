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
package org.atteo.config.jaxb;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import javax.xml.bind.annotation.XmlElement;

import org.atteo.filtering.PropertyFilter;
import org.atteo.filtering.PropertyNotFoundException;
import org.eclipse.persistence.jaxb.javamodel.reflection.AnnotationHelper;


/**
 * Custom JAXB annotation reader which filters {@link XmlElement}
 * {@link XmlElement#defaultValue() default values} with properties.
 */
public class FilteringAnnotationReader extends AnnotationHelper {
	private final PropertyFilter propertyFilter;

	public FilteringAnnotationReader(PropertyFilter propertyFilter) {
		this.propertyFilter = propertyFilter;
	}

	@Override
	public Annotation getAnnotation(AnnotatedElement elem, Class annotationClass) {
		if (!XmlElement.class.equals(annotationClass)) {
			return super.getAnnotation(elem, annotationClass);
		}

		final XmlElement original = (XmlElement) super.getAnnotation(elem, XmlElement.class);
		if (original == null) {
			return null;
		}

		return filterOriginal(original);
	}

	@Override
	public Annotation[] getAnnotations(AnnotatedElement elem) {
		Annotation[] original =  super.getAnnotations(elem);
		Annotation[] filtered = new Annotation[original.length];

		for (int i = 0; i < original.length; i++) {
			if (!XmlElement.class.equals(original[i].annotationType())) {
				filtered[i] = original[i];
			} else {
				filtered[i] = filterOriginal((XmlElement)original[i]);
			}
		}

		return filtered;
	}



	private Annotation filterOriginal(final XmlElement original) {
		final String defaultValue;
		try {
			String originalValue = original.defaultValue();
			if ("\u0000".equals(originalValue)) {
				return original;
			}
			defaultValue = propertyFilter.filter(originalValue);
			if (defaultValue == null || defaultValue.equals(originalValue)) {
				return original;
			}
		} catch (PropertyNotFoundException e) {
			return original;
		}

		return new XmlElement() {
			@Override
			public String name() {
				return original.name();
			}

			@Override
			public boolean nillable() {
				return original.nillable();
			}

			@Override
			public boolean required() {
				return original.required();
			}

			@Override
			public String namespace() {
				return original.namespace();
			}

			@Override
			public String defaultValue() {
				return defaultValue;
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Class type() {
				return original.type();
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return original.annotationType();
			}
		};
	}
}