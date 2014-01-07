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
package org.atteo.moonshine.services;

import java.util.Collections;

import javax.annotation.Nonnull;

import com.google.inject.Module;

public abstract class AbstractService implements Service {
	@Override
	public String getId() {
		return null;
	}

	@EmptyImplementation
	@Override
	public Module configure() {
		return null;
	}

	@EmptyImplementation
	@Override
	public void start() {
	}

	@EmptyImplementation
	@Override
	public void stop() {
	}

	@EmptyImplementation
	@Override
	public void close() {
	}

	@Nonnull
	@Override
	public Iterable<? extends Service> getSubServices() {
		return Collections.emptyList();
	}
}
