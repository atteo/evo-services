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
package org.atteo.moonshine.jersey;


import org.atteo.moonshine.jaxrs.JaxrsTest;
import org.atteo.moonshine.tests.MoonshineConfiguration;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <jersey>"
		+ "        <discoverResources>true</discoverResources>"
		+ "        <prefix>/rest</prefix>"
		+ "    </jersey>"
		+ "</config>")
public class JerseyTest extends JaxrsTest {
}
