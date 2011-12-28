/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ConfigurationTest {
	@Test
	public void trivial() throws IOException, IncorrectConfigurationException {
		String config =
				"<topLevel>"
				+ "<entry>"
				+ "<value>value</value>"
				+ "</entry>"
				+ "</topLevel>";
		TopLevel top = parse(config, config);
		Entry entry = (Entry) top.entries.get(0);
		assertEquals("value", entry.getValue());
	}

	@Test
	public void merge() throws IOException, IncorrectConfigurationException {
		String parent =
				"<topLevel>"
				+ "<entry>"
				+ "<value>value</value>"
				+ "</entry>"
				+ "</topLevel>";
		String child =
				"<topLevel>"
				+ "<entry>"
				+ "<value>value2</value>"
				+ "</entry>"
				+ "</topLevel>";
		TopLevel top = parse(parent, child);
		Entry entry = (Entry) top.entries.get(0);
		assertEquals("value2", entry.getValue());
	}

	@Test
	public void append() throws IOException, IncorrectConfigurationException {
		String parent =
				"<topLevel>"
				+ "<append>"
				+ "<entry>"
				+ "<value>value</value>"
				+ "</entry>"
				+ "</append>"
				+ "</topLevel>";
		String child =
				"<topLevel>"
				+ "<append>"
				+ "<entry>"
				+ "<value>value2</value>"
				+ "</entry>"
				+ "</append>"
				+ "</topLevel>";
		TopLevel top = parse(parent, child);
		assertEquals("value", top.append.get(0).getValue());
		assertEquals("value2", top.append.get(1).getValue());
	}

	@Test
	public void override() throws IOException, IncorrectConfigurationException {
		String parent =
				"<topLevel>"
				+ "<middle>"
				+ "<entry>"
				+ "<value>value</value>"
				+ "</entry>"
				+ "<entry>"
				+ "<value>value</value>"
				+ "</entry>"
				+ "</middle>"
				+ "</topLevel>";
		String child =
				"<topLevel>"
				+ "<middle>"
				+ "<entry>"
				+ "<value>value2</value>"
				+ "</entry>"
				+ "</middle>"
				+ "</topLevel>";
		TopLevel top = parse(parent, child);
		assertEquals(1, top.middle.entry.size());
		assertEquals("value2", top.middle.entry.get(0).getValue());
	}

	@Test
	public void defaults() throws IOException, IncorrectConfigurationException {
		String parent =
				"<topLevel>"
				+ "<append combine='MERGE'>"
				+ "<entry combine='DEFAULTS'>"
				+ "<value>value</value>"
				+ "</entry>"
				+ "</append>"
				+ "</topLevel>";
		String child =
				"<topLevel>"
				+ "<append>"
				+ "<entry>"
				+ "</entry>"
				+ "</append>"
				+ "</topLevel>";
		TopLevel top = parse(parent, child);
		assertEquals("value", top.append.get(0).getValue());

		String child2 =
				"<topLevel>"
				+ "<append>"
				+ "</append>"
				+ "</topLevel>";
		top = parse(parent, child2);
		assertTrue(top.append.isEmpty());
	}

	@Test(expected = IncorrectConfigurationException.class)
	public void invalidId() throws IOException, IncorrectConfigurationException {
		String config =
				"<topLevel>"
				+ "<entry id='!@'>"
				+ "<value>value</value>"
				+ "</entry>"
				+ "</topLevel>";
		parse(config, config);
	}

	@Test
	public void defaultValues() throws IOException, IncorrectConfigurationException {
		String config =
				"<topLevel>"
				+ "<entry>"
				+ "<intValue/>"
				+ "<intValue2/>"
				+ "</entry>"
				+ "</topLevel>";
		TopLevel result = parse(config, config);
		assertEquals(5, result.entries.get(0).getIntValue());
		assertEquals(5, result.entries.get(0).getIntValue2());
		assertEquals(5, result.entries.get(0).getIntValue3());
	}

	private TopLevel parse(String parent, String child) throws IOException,
			IncorrectConfigurationException {
		Configuration configuration = new Configuration();
		Properties properties = new Properties();
		properties.setProperty("intValue", "5");
		configuration.setProperties(properties);
		return configuration.read(TopLevel.class,
				new ByteArrayInputStream(parent.getBytes("UTF-8")),
				new ByteArrayInputStream(child.getBytes("UTF-8")));
	}
}
