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
