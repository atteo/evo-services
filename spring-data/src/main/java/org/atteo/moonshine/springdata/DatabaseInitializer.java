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
package org.atteo.moonshine.springdata;

import javax.inject.Provider;

/**
 * Initializes the database.
 *
 * <p>
 * Bind the {@link Provider} of this class to register database initialization logic.
 * {@link RepositoryFactoryProvider} depends on this class, so object of this class will be built
 * before database is accessed by Sprint Data created repositories.
 * </p>
 */
public interface DatabaseInitializer {

}
