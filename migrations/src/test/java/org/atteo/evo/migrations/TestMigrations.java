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
package org.atteo.evo.migrations;

import javax.inject.Inject;

import org.atteo.evo.tests.RequestRule;
import org.atteo.evo.tests.ServicesRule;
import org.junit.Rule;
import org.junit.Test;

public class TestMigrations {
	@Rule
	public ServicesRule servicesRule = new ServicesRule(this, "/test-config.xml");

	@Rule
	public RequestRule requestRule = new RequestRule();

	@Inject
	private Migrations migrations;
	
	@Test
	public void testMigrations() {
		migrations.migrate("test-migration1.xml");
		migrations.migrate("test-migration2.xml");
	}
}
