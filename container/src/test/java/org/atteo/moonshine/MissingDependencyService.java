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

package org.atteo.moonshine;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.inject.Module;

@XmlRootElement(name = "missing-dependency-service")
@ServiceConfiguration(auto = false)
public class MissingDependencyService extends TopLevelService {
	@Inject
	private String someNotExistingBinding;

	static int configureCount = 0;
	static int closeCount = 0;

	@Override
	public Module configure() {
		configureCount++;
		return null;
	}

	@Override
	public void close() {
		closeCount++;
	}
}
