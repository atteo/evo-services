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
package org.atteo.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
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
		Entry entry = top.entries.get(0);
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
		Entry entry = top.entries.get(0);
		assertEquals("value2", entry.getValue());
	}

	@Test
	public void append() throws IOException, IncorrectConfigurationException {
		String parent =
				"<topLevel>"
				+ "<append combine.children='append'>"
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
				+ "<middle combine.self='override'>"
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
				+ "<append combine.children='MERGE'>"
				+ "<entry combine.self='DEFAULTS'>"
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
				+ "<intValue4>4</intValue4>"
				+ "</entry>"
				+ "</topLevel>";
		TopLevel result = parse(config, config);
		assertEquals(5, result.entries.get(0).getIntValue());
		// TODO:
		//assertEquals(5, result.entries.get(0).getIntValue2());
		assertEquals(5, result.entries.get(0).getIntValue3());
		assertEquals(4, result.entries.get(0).getIntValue4());
		assertEquals(true, result.entries.get(0).getBooleanValue());
	}

	@Test
	public void idRef() throws IOException, IncorrectConfigurationException {
		String config =
				"<topLevel specialMiddle='test2'>"
				+ "<entry id='test'>"
				+ "<intValue/>"
				+ "</entry>"
				+ "<entry id='test2'>"
				+ "<intValue/>"
				+ "</entry>"
				+ "<middle id='test2'/>"
				+ "</topLevel>";
		TopLevel result = parse(config, config);
		assertTrue(result.specialMiddle == result.middle);
	}

	@Test(expected = IncorrectConfigurationException.class)
	// TODO
	@Ignore
	public void notUniqueIdRef() throws IOException, IncorrectConfigurationException {
		String config =
				"<topLevel specialEntry='test2'>"
				+ "<entry id='test2'>"
				+ "</entry>"
				+ "<middle id='test2'/>"
				+ "</topLevel>";
		parse(config, config);
	}

	// Test workaround for: https://java.net/jira/browse/JAXB-974
	@Test
	public void shouldGenerateSchemaWhenFileNameContainsSpace() throws IOException {
		// given
		Path path = Paths.get("target/folder with spaces/");
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		Path file = path.resolve("file with spaces.xsd");
		Files.deleteIfExists(file);
		Configuration configuration = new Configuration();
		configuration.generateSchema(file.toFile());

		// then
		assertThat(file.toFile()).exists();
	}

	@Test
	public void shouldHandleIntegerProperties() throws IOException, IncorrectConfigurationException {
		// given
		String config = ""
			+ "<topLevel>"
			+ "    <entry>"
			+ "        <intValue>${intValue}</intValue>"
			+ "    </entry>"
			+ "</topLevel>";

		// when
		TopLevel result = parse(config, config);

		// then
		assertThat(result.entries.get(0).intValue).isEqualTo(5);
	}

	private TopLevel parse(String... documents) throws IOException,
			IncorrectConfigurationException {
		Configuration configuration = new Configuration();
		for (int i = 0; i < documents.length; i++) {
			InputStream stream = new ByteArrayInputStream(documents[i].getBytes(StandardCharsets.UTF_8));
			configuration.combine(stream);
		}
		Properties properties = new Properties();
		properties.setProperty("intValue", "5");
		configuration.filter(properties);
		return configuration.read(TopLevel.class);
	}
}
