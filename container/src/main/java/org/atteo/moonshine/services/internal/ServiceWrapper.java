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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.moonshine.reflection.ReflectionUtils;
import org.atteo.moonshine.services.EmptyImplementation;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.services.ServiceInfo;
import org.atteo.moonshine.services.ServiceMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Module;
import com.google.inject.spi.Element;

public class ServiceWrapper implements ServiceInfo, ServiceMXBean, MBeanRegistration {
	private final Logger logger = LoggerFactory.getLogger("Moonshine");
	private final String name;
	private final ObjectName objectName;
	private final Service service;
	private final List<Dependency> dependencies = new ArrayList<>();
	private final AtomicReference<Status> status = new AtomicReference<>(Status.CREATED);
	private List<com.google.inject.spi.Element> elements;
	private boolean singleton = false;
	private boolean configureImplemented;
	private boolean startImplemented;
	private boolean stopImplemented;
	private boolean closeImplemented;

	public ServiceWrapper(Service service) {
		this.service = service;
		this.name = getServiceName(service);
		this.objectName = getObjectName(service);
		this.configureImplemented = isImplemented(service.getClass(), "configure");
		this.startImplemented = isImplemented(service.getClass(), "start");
		this.stopImplemented = isImplemented(service.getClass(), "stop");
		this.closeImplemented = isImplemented(service.getClass(), "close");
	}

	public void addDependency(ServiceWrapper service, Annotation annotation) {
		dependencies.add(new Dependency(service, annotation));
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setElements(List<Element> elements) {
		this.elements = elements;
	}

	public Status getStatus() {
		return status.get();
	}

	@Override
	public List<Element> getElements() {
		return elements;
	}

	private static String getServiceName(Service service) {
		StringBuilder builder = new StringBuilder();

		if (service.getId() != null) {
			builder.append("\"");
			builder.append(service.getId());
			builder.append("\" ");
		}

		String className = service.getClass().getSimpleName();
		if (Strings.isNullOrEmpty(className)) {
			// for anonymous inner class getSimpleName() returns ""
			className = service.getClass().getName();
		}
		builder.append(className);
		String summary = ClassIndex.getClassSummary(service.getClass());
		if (summary != null) {
			builder.append(" (");
			builder.append(summary);
			builder.append(")");
		}
		return builder.toString();
	}

	public static ObjectName getObjectName(Service service) {
		try {
			Hashtable<String, String> keys = new Hashtable<>();
			keys.put("type", service.getClass().getName());
			if (service.getId() != null) {
				keys.put("id", service.getId());
			}
			return ObjectName.getInstance("org.atteo.moonshine.services", keys);
		} catch (MalformedObjectNameException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static boolean isImplemented(Class<?> klass, String methodName) {
		Method method = ReflectionUtils.findMethod(klass, methodName);

		return method.getAnnotation(EmptyImplementation.class) == null;
	}

	public boolean isSingleton() {
		return singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	private void changeState(Status expect, Status update) {
		if (!status.compareAndSet(expect, update)) {
			throw new IllegalStateException("Cannot configure service, not in " + expect + " state");
		}
	}

	public Module configure() {
		changeState(Status.CREATED, Status.CONFIGURING);
		if (logger.isInfoEnabled() && configureImplemented) {
			logger.info("Configuring: {}", getName());
		}
		Module module = service.configure();
		status.set(Status.READY);
		return module;
	}

	@Override
	public void start() {
		changeState(Status.READY, Status.STARTING);
		if (logger.isInfoEnabled() && startImplemented) {
			logger.info("Starting: {}", getName());
		}
		service.start();
		status.set(Status.STARTED);
	}

	@Override
	public void stop() {
		if (!status.compareAndSet(Status.STARTED, Status.STOPPING)) {
			return;
		}
		if (logger.isInfoEnabled() && stopImplemented) {
			logger.info("Stopping: {}", getName());
		}
		try {
			service.stop();
		} finally {
			status.set(Status.READY);
		}
	}

	public void close() {
		if (!status.compareAndSet(Status.READY, Status.CLOSING)) {
			return;
		}
		if (logger.isInfoEnabled() && closeImplemented) {
			logger.info("Closing: {}", getName());
		}
		try {
			service.close();
		} finally {
			status.set(Status.CLOSED);
		}
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	@Override
	public ObjectName preRegister(MBeanServer mbs, ObjectName on) throws Exception {
		return objectName;
	}

	@Override
	public void postRegister(Boolean bln) {
	}

	@Override
	public void preDeregister() throws Exception {
	}

	@Override
	public void postDeregister() {
	}

	public static class Dependency {
		private final ServiceWrapper service;
		private final Annotation annotation;

		public Dependency(ServiceWrapper service, Annotation annotation) {
			this.service = service;
			this.annotation = annotation;
		}

		public Annotation getAnnotation() {
			return annotation;
		}

		public ServiceWrapper getService() {
			return service;
		}
	}

	public static enum Status {
		CREATED, CONFIGURING, READY, STARTING, STARTED, STOPPING, CLOSING, CLOSED
	}
}
