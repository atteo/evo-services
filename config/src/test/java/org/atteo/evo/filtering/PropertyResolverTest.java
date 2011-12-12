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
package org.atteo.evo.filtering;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PropertyResolverTest {
	@Test
	public void system() throws PropertyNotFoundException {
		System.setProperty("testKey", "testValue");

		assertEquals("system: testValue", Filtering.filter("system: ${testKey}",
				new SystemPropertyResolver()));
	}

	@Test
	public void env() throws PropertyNotFoundException {
		Filtering.filter("env: ${env.PATH}", new EnvironmentPropertyResolver());
	}

	@Test
	public void compound() throws PropertyNotFoundException {
		PropertyResolver resolver = new CompoundPropertyResolver(
				new SystemPropertyResolver(),
				new EnvironmentPropertyResolver());

		Filtering.filter("system: ${testKey}, env: ${env.PATH}", resolver);
	}

	@Test
	public void recursion() throws PropertyNotFoundException {
		System.setProperty("first", "value");
		System.setProperty("second", "${first} ${first}");
		System.setProperty("third", "${first} ${second}");

		PropertyResolver resolver = new RecursivePropertyResolver(new SystemPropertyResolver());
		assertEquals("value value value", Filtering.filter("${third}", resolver));
	}

	@Test(expected = RuntimeException.class)
	public void cycle() throws PropertyNotFoundException {
		System.setProperty("first", "${third}");
		System.setProperty("second", "${first} ${first}");
		System.setProperty("third", "${first} ${second}");

		PropertyResolver resolver = new RecursivePropertyResolver(new SystemPropertyResolver());
		Filtering.filter("${third}", resolver);
	}

	@Test
	public void xml() throws ParserConfigurationException, SAXException, IOException {
		String xml = "<config>"
				+ "<a value='test'/>"
				+ "<b>test2</b>"
				+ "<c><d>test3</d></c>"
				+ "<e><f>test4</f><f>test5</f></e>"
				+ "<g.h><i>test6</i></g.h>"
				+ "</config>";

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

		PropertyResolver resolver = new XmlPropertyResolver(document.getDocumentElement(), true);
		assertEquals("test", resolver.getProperty("config.a.value"));
		assertEquals("test2", resolver.getProperty("config.b"));
		assertEquals("test3", resolver.getProperty("config.c.d"));
		assertNull(resolver.getProperty("config.e.f"));
		assertEquals("test6", resolver.getProperty("config.g.h.i"));

		resolver = new XmlPropertyResolver(document.getDocumentElement(), false);
		assertEquals("test", resolver.getProperty("a.value"));
		assertEquals("test6", resolver.getProperty("g.h.i"));
	}
}
