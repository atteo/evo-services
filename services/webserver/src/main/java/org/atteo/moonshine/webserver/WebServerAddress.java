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

/**
 * Interface which can be injected to retrieve webserver address.
 */
public interface WebServerAddress {
	/**
	 * Returns the port web server listens on.
	 * @return opened port or -1, if not yet opened
	 */
	int getPort();

	/**
	 * Returns the host name the web server listens on.
	 *
	 * @return host, or null if web server listens on all interfaces
	 */
	String getHost();

	/**
	 * Returns the URL web server listens on.
	 */
	String getUrl();
}
