package org.atteo.evo.hsqldb;

import org.atteo.moonshine.database.DatabaseTest;
import org.atteo.moonshine.tests.MoonshineConfiguration;

@MoonshineConfiguration(fromString = "<config>"
		+ "    <hsqldb/>"
		+ "</config>")
public class HsqldbTest extends DatabaseTest {
}
