/*
 * Copyright 2012 Atteo.
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
package org.atteo.moonshine.tests;

import org.junit.runner.RunWith;

/**
 * Executed the test class inside Moonshine container.
 *
 * <p>
 * You can configure the container by annotating the class with {@link MoonshineConfiguration}.
 * </p>
 * <p>
 * If you don't want to extend this class you can equivalently annotate your class with
 * &#064;RunWith({@link MoonshineRunner}.class).
 * </p>
 */
@RunWith(MoonshineRunner.class)
public class MoonshineTest {
}
