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
package org.atteo.moonshine.jmx;

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * MBean which lists all registered Guice bindings.
 */
// TODO: also show private injectors content
@MBean
public class GuiceBindings implements DynamicMBean {
	private Injector injector;

	@Inject
	private void setInjector(Injector injector) {
		// Services injects this service private injector, we want to show the keys from the global
		this.injector = injector.getParent();
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException,
			ReflectionException {

		for (Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet()) {
			Key<?> key = entry.getKey();
			Binding<?> binding = entry.getValue();

			if (attribute.equals(key.toString())) {
				return binding.getProvider().toString() + " at " + binding.getSource().toString();
			}
		}
		return null;
	}

	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		AttributeList list = new AttributeList(injector.getAllBindings().size());

		for (Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet()) {
			Key<?> key = entry.getKey();

			list.add(new Attribute(key.toString(), key));
		}

		return list;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[injector.getAllBindings().size()];
		int i = 0;
		for (Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet()) {
			Key<?> key = entry.getKey();
			attributes[i] = new MBeanAttributeInfo(key.toString(), "java.lang.String",
					"Key " + key.toString(), true, false, false);
			i++;
		}

		return new MBeanInfo(GuiceBindings.class.getName(), "Guice Bindings", attributes, null,
				null, null);
	}

}
