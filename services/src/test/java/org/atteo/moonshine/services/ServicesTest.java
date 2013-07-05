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
package org.atteo.moonshine.services;

import java.io.File;
import java.io.IOException;

import org.atteo.evo.config.IncorrectConfigurationException;
import org.junit.Test;

public class ServicesTest {
	@Test
	public void empty() {
		Services services = new Services();
		services.start();
		services.stop();
	}

	@Test
	public void simple() throws IncorrectConfigurationException, IOException {
		Services services = new Services();
		services.setHomeDirectory(new File("target/test-home/"));
		services.combineConfigurationFromResource("/simple.xml", true);

		services.start();
		services.stop();
	}

	@Test(expected = RuntimeException.class)
	public void singletonWithId() throws IncorrectConfigurationException, IOException {
		Services services = new Services();
		services.setHomeDirectory(new File("target/test-home/"));
		services.combineConfigurationFromResource("/singleton-with-id.xml", true);

		services.start();
		services.stop();
	}

	@Test
	public void twoLegRobot() throws IncorrectConfigurationException, IOException {
		Services services = new Services();
		services.setHomeDirectory(new File("target/test-home/"));
		services.combineConfigurationFromResource("/robot-service.xml", true);

		services.start();
		services.injector().getInstance(Robot.class);
		services.stop();
	}
}
