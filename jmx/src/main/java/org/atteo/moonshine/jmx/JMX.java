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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.classindex.ClassIndex;
import org.atteo.moonshine.TopLevelService;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * JMX helper service.
 *
 * <p>
 * Starts MBean server and automatically registers any class annotated with &#064;{@link MBean}.
 * </p>
 */
@XmlRootElement(name = "jmx")
public class JMX extends TopLevelService {
	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());

				for (Class<?> klass : ClassIndex.getAnnotated(MBean.class)) {
					bind(klass).in(Singleton.class);
				}
			}
		};
	}

	@Inject
	private Injector injector;

	@Inject
	private MBeanServer server;

	private List<ObjectName> registeredNames = new ArrayList<>();

	@Override
	public void start() {
		try {
			for (Class<?> klass : ClassIndex.getAnnotated(MBean.class)) {
				Object instance = injector.getInstance(klass);
				ObjectName name;
				MBean annotation = klass.getAnnotation(MBean.class);
				if (annotation != null && !"".equals(annotation.name())) {
					name = ObjectName.getInstance(annotation.name());
				} else {
					name = ObjectName.getInstance(klass.getPackage().getName() + ":type="
							+ klass.getSimpleName());
				}
				ObjectInstance mbeanInstance = server.registerMBean(instance, name);
				registeredNames.add(mbeanInstance.getObjectName());
			}
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
				| NotCompliantMBeanException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		for (ObjectName name : registeredNames) {
			try {
				server.unregisterMBean(name);
			} catch (InstanceNotFoundException e) {
				// not found? ignore
				continue;
			} catch (MBeanRegistrationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
