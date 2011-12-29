package org.atteo.evo.h2;

import org.atteo.evo.database.DatabaseTest;
import org.atteo.evo.tests.ServicesRule;
import org.junit.Rule;

public class H2Test extends DatabaseTest {
	@Rule
	public ServicesRule rule = new ServicesRule("/test-config.xml");
}
