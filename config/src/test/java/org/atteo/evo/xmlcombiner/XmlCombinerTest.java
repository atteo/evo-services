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
package org.atteo.evo.xmlcombiner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.Diff;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLIdentical;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlCombinerTest {
	@Test
	public void same() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String parent = ""
				+ "<outer>"
				+ "  <inner attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "</outer>";
		String child = ""
				+ "<outer>"
				+ "  <inner attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "</outer>";
		String result = ""
				+ "<outer>"
				+ "  <inner attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(result, combine(parent, child)), true);

		String child2 = ""
				+ "<outer combine='APPEND'>"
				+ "  <inner combine='APPEND' attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "</outer>";
		String result2 = ""
				+ "<outer combine='APPEND'>"
				+ "  <inner attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "  <inner combine='APPEND' attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(result2, combine(parent, child2)), true);

		String child3 = ""
				+ "<outer combine='OVERRIDE'>"
				+ "  <inner combine='OVERRIDE' attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "</outer>";
		String result3 = ""
				+ "<outer combine='OVERRIDE'>"
				+ "  <inner combine='OVERRIDE' attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(result3, combine(parent, child3)), true);
	}

	@Test
	public void attributes() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String parent = ""
				+ "<outer>"
				+ "  <inner attribute='value' other_attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "</outer>";
		String child = ""
				+ "<outer>"
				+ "  <inner attribute='value2'>"
				+ "    content2"
				+ "  </inner>"
				+ "</outer>";
		String result = ""
				+ "<outer>"
				+ "  <inner other_attribute='value' attribute='value2'>"
				+ "    content2"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(result, combine(parent, child)), true);

		String child2 = ""
				+ "<outer combine='APPEND'>"
				+ "  <inner combine='APPEND' attribute='value2'>"
				+ "    content2"
				+ "  </inner>"
				+ "</outer>";
		String result2 = ""
				+ "<outer combine='APPEND'>"
				+ "  <inner other_attribute='value' attribute='value'>"
				+ "    content"
				+ "  </inner>"
				+ "  <inner combine='APPEND' attribute='value2'>"
				+ "    content2"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(result2, combine(parent, child2)), true);

		String child3 = ""
				+ "<outer combine='OVERRIDE'>"
				+ "  <inner combine='OVERRIDE' attribute='value2'>"
				+ "    content2"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(child3, combine(parent, child3)), true);
	}

	@Test
	public void subnodes() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String parent = ""
				+ "<outer>"
				+ "  <inner>"
				+ "    content"
				+ "  </inner>"
				+ "  <inner2>"
				+ "    content2"
				+ "  </inner2>"
				+ "</outer>";
		String child = ""
				+ "<outer>"
				+ "  <inner>"
				+ "    content3"
				+ "  </inner>"
				+ "</outer>";
		String result = ""
				+ "<outer>"
				+ "<inner2>"
				+ "    content2"
				+ "  </inner2>"
				+ "  <inner>"
				+ "    content3"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(result, combine(parent, child)), true);

		String child2 = ""
				+ "<outer combine='APPEND'>"
				+ "  <inner>"
				+ "    content3"
				+ "  </inner>"
				+ "</outer>";
		String result2 = ""
				+ "<outer combine='APPEND'>"
				+ "  <inner>"
				+ "    content"
				+ "  </inner>"
				+ "  <inner2>"
				+ "    content2"
				+ "  </inner2>"
				+ "  <inner>"
				+ "    content3"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(result2, combine(parent, child2)), true);

		String child3 = ""
				+ "<outer combine='OVERRIDE'>"
				+ "  <inner>"
				+ "    content3"
				+ "  </inner>"
				+ "</outer>";
		assertXMLIdentical(new Diff(child3, combine(parent, child3)), true);
	}

	@Test
	public void remove() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String parent = "<a a=\"a1\"><b></b></a>";
		String child = "<a b=\"b2\"><b combine=\"REMOVE\"></b><c></c></a>";
		String result = "<a a=\"a1\" b=\"b2\"><c></c></a>";

		assertXMLIdentical(new Diff(result, combine(parent, child)), true);
	}

	@Test
	public void withId() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String parent = "<a><b id=\"1\" a=\"a\"><c/></b><b id=\"2\"><c></c></b></a>";
		String child = "<a><b id=\"1\" a=\"a\"><d/></b><b id=\"3\"><c></c></b></a>";
		String result = "<a><b id=\"2\"><c></c></b><b id=\"1\" a=\"a\"><c/><d/></b><b id=\"3\"><c></c></b></a>";

		assertXMLIdentical(new Diff(result, combine(parent, child)), true);
	}

	private static String combine(String parent, String child) throws IOException,
			ParserConfigurationException, SAXException, TransformerConfigurationException,
			TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document parentDocument = builder.parse(new ByteArrayInputStream(parent.getBytes("UTF-8")));
		Document childDocument = builder.parse(new ByteArrayInputStream(child.getBytes("UTF-8")));
		Document result = XmlCombiner.combine(builder, parentDocument, childDocument);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(result), new StreamResult(writer));
		return writer.toString();
	}
}
