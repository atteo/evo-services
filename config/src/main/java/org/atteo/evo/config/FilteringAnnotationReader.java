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
package org.atteo.evo.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;

import org.atteo.evo.filtering.Filtering;
import org.atteo.evo.filtering.PropertiesPropertyResolver;
import org.atteo.evo.filtering.PropertyNotFoundException;
import org.atteo.evo.filtering.PropertyResolver;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import com.sun.xml.bind.v2.model.core.ErrorHandler;

/**
 * Custom JAXB annotation reader which filters {@link XmlElement}
 * {@link XmlElement#defaultValue() default values} with properties.
 */
public class FilteringAnnotationReader implements RuntimeAnnotationReader {
	private RuntimeAnnotationReader delegate = new RuntimeInlineAnnotationReader();
	private PropertyResolver propertyResolver;

	public FilteringAnnotationReader(PropertyResolver propertyResolver) {
		this.propertyResolver = propertyResolver;
	}

	public FilteringAnnotationReader(Properties properties) {
		this(new PropertiesPropertyResolver(properties));
	}

	@Override
	public void setErrorHandler(ErrorHandler errorHandler) {
		delegate.setErrorHandler(errorHandler);
	}

	@Override
	public <A extends Annotation> A getFieldAnnotation(Class<A> annotation, Field field,
			Locatable srcpos) {
		A original = delegate.getFieldAnnotation(annotation, field, srcpos);
		if (annotation == XmlElement.class) {
			final XmlElement xmlElement = (XmlElement) original;
			final String defaultValue;
			try {
				String originalValue = xmlElement.defaultValue();
				if ("\u0000".equals(originalValue)) {
					return original;
				}
				defaultValue = Filtering.filter(originalValue, propertyResolver);
				if (defaultValue == null || defaultValue.equals(originalValue)) {
					return original;
				}
			} catch (PropertyNotFoundException e) {
				return original;
			}

			return annotation.cast(new XmlElement() {
				@Override
				public String name() {
					return xmlElement.name();
				}

				@Override
				public boolean nillable() {
					return xmlElement.nillable();
				}

				@Override
				public boolean required() {
					return xmlElement.required();
				}

				@Override
				public String namespace() {
					return xmlElement.namespace();
				}

				@Override
				public String defaultValue() {
					return defaultValue;
				}

				@SuppressWarnings("rawtypes")
				@Override
				public Class type() {
					return xmlElement.type();
				}

				@Override
				public Class<? extends Annotation> annotationType() {
					return xmlElement.annotationType();
				}
			});
		}
		return original;
	}

	@Override
	public boolean hasFieldAnnotation(Class<? extends Annotation> annotationType, Field field) {
		return delegate.hasFieldAnnotation(annotationType, field);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean hasClassAnnotation(Class clazz, Class<? extends Annotation> annotationType) {
		return delegate.hasClassAnnotation(clazz, annotationType);
	}

	@Override
	public Annotation[] getAllFieldAnnotations(Field field, Locatable srcPos) {
		return delegate.getAllFieldAnnotations(field, srcPos);
	}

	@Override
	public <A extends Annotation> A getMethodAnnotation(Class<A> annotation, Method getter,
			Method setter, Locatable srcpos) {
		return delegate.getMethodAnnotation(annotation, getter, setter, srcpos);
	}

	@Override
	public boolean hasMethodAnnotation(Class<? extends Annotation> annotation, String propertyName,
			Method getter, Method setter, Locatable srcPos) {
		return delegate.hasMethodAnnotation(annotation, propertyName, getter, setter, srcPos);
	}

	@Override
	public Annotation[] getAllMethodAnnotations(Method method, Locatable srcPos) {
		return delegate.getAllMethodAnnotations(method, srcPos);
	}

	@Override
	public <A extends Annotation> A getMethodAnnotation(Class<A> annotation, Method method,
			Locatable srcpos) {
		return delegate.getMethodAnnotation(annotation, method, srcpos);
	}

	@Override
	public boolean hasMethodAnnotation(Class<? extends Annotation> annotation, Method method) {
		return delegate.hasMethodAnnotation(annotation, method);
	}

	@Override
	public <A extends Annotation> A getMethodParameterAnnotation(Class<A> annotation,
			Method method, int paramIndex, Locatable srcPos) {
		return delegate.getMethodParameterAnnotation(annotation, method, paramIndex, srcPos);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <A extends Annotation> A getClassAnnotation(Class<A> annotation, Class clazz,
			Locatable srcpos) {
		return delegate.getClassAnnotation(annotation, clazz, srcpos);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <A extends Annotation> A getPackageAnnotation(Class<A> annotation, Class clazz,
			Locatable srcpos) {
		return delegate.getPackageAnnotation(annotation, clazz, srcpos);
	}

	@Override
	public Type getClassValue(Annotation a, String name) {
		return delegate.getClassValue(a, name);
	}

	@Override
	public Type[] getClassArrayValue(Annotation a, String name) {
		return delegate.getClassArrayValue(a, name);
	}
}
