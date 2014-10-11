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

import java.util.Map;

import javax.inject.Provider;

public class ServletOrFilterDefinition<T> implements Comparable<ServletOrFilterDefinition<?>> {

	private final String[] patterns;

	private final Provider<T> servlet;

	private final Map<String, String> params;

	private final int priority;

	public ServletOrFilterDefinition(Provider<T> servlet, Map<String, String> params, String[] patterns, int priority) {
		this.patterns = patterns;
		this.servlet = servlet;
		this.params = params;
		this.priority = priority;
	}

	public String[] getPatterns() {
		return patterns;
	}

	public Provider<T> getProvider() {
		return servlet;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(ServletOrFilterDefinition<?> o) {
		return ((Integer) priority).compareTo(o.getPriority());
	}
}
