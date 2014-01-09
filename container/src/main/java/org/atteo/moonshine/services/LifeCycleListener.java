/*
 * Copyright 2014 Atteo.
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

import com.google.inject.Injector;

import java.util.EventListener;

/**
 * Moonshine life cycle listener.
 */
public interface LifeCycleListener extends EventListener {
	/**
	 * Executed after all services {@link Service#configure() configure} successfully.
	 * @param injector Global injector
	 */
	void configured(Injector injector);

	/**
	 * Executed after all services {@link Service#start() start} successfully.
	 */
	void started();

	/**
	 * Executed before all services are going to be {@link Service#stop() stopped}.
	 */
	void stopping();

	/**
	 * Executed before all services are going to be {@link Service#close() closed}.
	 */
	void closing();
}
