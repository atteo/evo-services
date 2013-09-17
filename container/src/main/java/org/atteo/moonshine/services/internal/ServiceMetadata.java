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
import java.util.ArrayList;
import java.util.List;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.moonshine.services.Service;
import org.atteo.moonshine.services.ServiceInfo;

import com.google.inject.spi.Element;

public class ServiceMetadata implements ServiceInfo {
	private final String name;
	private final Service service;
	private final List<Dependency> dependencies = new ArrayList<>();
	private Status status = Status.CREATED;
	private List<com.google.inject.spi.Element> elements;
	private boolean singleton = false;

	public ServiceMetadata(Service service) {
		this.service = service;
		this.name = getServiceName(service);
	}

	public void addDependency(ServiceMetadata service, Annotation annotation) {
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
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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

		String summary = ClassIndex.getClassSummary(service.getClass());
		builder.append(service.getClass().getSimpleName());
		if (summary != null) {
			builder.append(" (");
			builder.append(summary);
			builder.append(")");
		}
		return builder.toString();
	}

	public boolean isSingleton() {
		return singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public static class Dependency {
		private final ServiceMetadata service;
		private final Annotation annotation;

		public Dependency(ServiceMetadata service, Annotation annotation) {
			this.service = service;
			this.annotation = annotation;
		}

		public Annotation getAnnotation() {
			return annotation;
		}

		public ServiceMetadata getService() {
			return service;
		}
	}

	public static enum Status {
		CREATED, READY, STARTED, CLOSED
	}
}
