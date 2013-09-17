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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.moonshine.services.TopLevelService;

import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

/**
 * Registers annotated servlets ands filters.
 *
 * <p>
 * Supports {@link WebServlet}, {@link WebFilter} and {@link WebInitParam} annotations
 * by automatically registering annotated classes using {@link ServletModule}.
 * </p>
 */
@XmlRootElement(name = "web-annotations")
@Singleton
public class WebAnnotationsService extends TopLevelService {

	@Override
	public Module configure() {
		return new ServletModule() {

			@Override
			protected void configureServlets() {
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

					String[] restOfUrls = Arrays.copyOfRange(urls, 1, urls.length);
					@SuppressWarnings("unchecked")
					Class<? extends HttpServlet> servletClass = (Class<? extends HttpServlet>) klass;
					serve(urls[0], restOfUrls).with(servletClass, params);
					bind(servletClass).in(Singleton.class);
				}
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

					String[] restOfUrls = Arrays.copyOfRange(urls, 1, urls.length - 1);
					@SuppressWarnings("unchecked")
					Class<? extends Filter> filterClass = (Class<? extends Filter>) klass;
					filter(urls[0], restOfUrls).through(filterClass, params);
					bind(filterClass).in(Singleton.class);
				}
			}
		};
	}
}
