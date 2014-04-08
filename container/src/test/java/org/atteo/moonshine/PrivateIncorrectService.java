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

import javax.xml.bind.annotation.XmlRootElement;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.name.Names;

@XmlRootElement(name = "incorrect-private")
@ServiceConfiguration(auto = false)
public class PrivateIncorrectService extends TopLevelService {
	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				bind(String.class).annotatedWith(Names.named("dummy2")).toInstance("dummy2");
				expose(Key.get(String.class, Names.named("dummy2")));
			}
		};
	}
}
