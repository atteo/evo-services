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

package org.atteo.moonshine.webserver;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

/**
 * Registers annotated servlets and filters.
 *
 * <p>
 * Supports {@link WebServlet}, {@link WebFilter} and {@link WebInitParam} annotations
 * by automatically registering annotated classes using {@link ServletModule}.
 * </p>
 */
@XmlRootElement(name = "web-annotations")
@Singleton
public class WebAnnotationsService extends TopLevelService {
	@XmlElement
	@XmlIDREF
	@ImportService
	private ServletRegistry servletContainer;

	@Override
	public Module configure() {
		return new PrivateModule() {

			@Override
			protected void configure() {
				for (Class<?> klass : ClassIndex.getAnnotated(WebFilter.class)) {
					WebFilter annotation = klass.getAnnotation(WebFilter.class);
					String[] urls = annotation.value();
					if (urls.length == 0) {
						urls = annotation.urlPatterns();
					}

					Map<String, String> params = new HashMap<>();

					for (WebInitParam param : annotation.initParams()) {
						params.put(param.name(), param.value());
					}

					@SuppressWarnings("unchecked")
					Class<Filter> filterClass = (Class<Filter>) klass;
					bind(filterClass).in(Singleton.class);

					for (String url : urls) {
						servletContainer.addFilter(url, filterClass, getProvider(filterClass), params);
					}
				}
				for (Class<?> klass : ClassIndex.getAnnotated(WebServlet.class)) {
					WebServlet annotation = klass.getAnnotation(WebServlet.class);
					String[] urls = annotation.value();
					if (urls.length == 0) {
						urls = annotation.urlPatterns();
					}

					Map<String, String> params = new HashMap<>();

					for (WebInitParam param : annotation.initParams()) {
						params.put(param.name(), param.value());
					}

					@SuppressWarnings("unchecked")
					Class<Servlet> servletClass = (Class<Servlet>) klass;
					bind(servletClass).in(Singleton.class);

					for (String url : urls) {
						servletContainer.addServlet(url, servletClass, getProvider(servletClass), params);
					}
				}
			}
		};
	}
}
