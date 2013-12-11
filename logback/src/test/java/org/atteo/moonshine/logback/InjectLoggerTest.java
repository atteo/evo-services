/*
 * Copyright 2011 Atteo.
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
package org.atteo.moonshine.logback;

import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Test;
import org.slf4j.Logger;
import static org.assertj.core.api.Assertions.assertThat;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
        + "     <jmx/>"
		+ "    <logback/>"
		+ "</config>")
public class InjectLoggerTest extends MoonshineTest {
    @Log
    Logger log;

    @Test
    public void testLoggerInjection() {
        assertThat(log).isNotNull();
        assertThat(log.getName()).isEqualTo(InjectLoggerTest.class.getName());
    }
}
