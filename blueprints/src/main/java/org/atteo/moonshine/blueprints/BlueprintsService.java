/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.blueprints;

import org.atteo.moonshine.TopLevelService;

/**
 * Blueprints DB abstraction service.
 * <p>
 * Database services that support Blueprints API should inherit from this service and bind
 * some Blueprints implementation that implements {@link com.tinkerpop.blueprints.Graph}.
 * </p>
 */
public abstract class BlueprintsService extends TopLevelService {

}
