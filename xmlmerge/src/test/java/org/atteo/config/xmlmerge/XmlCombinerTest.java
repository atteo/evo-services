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
package org.atteo.config.xmlmerge;

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
	public void identity() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String content = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "    </service>\n"
				+ "</config>";
		assertXMLIdentical(new Diff(content, combine(content, content)), true);
	}

	@Test
	public void mergeChildren() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>other value</parameter>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>other value</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "</config>";
		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);
	}

	@Test
	public void appendChildren() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='1' combine.children='append'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>other value</parameter>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <service id='1' combine.children='append'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "        <parameter>other value</parameter>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "</config>";
		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);
	}

	@Test
	public void commentPropagation() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <!-- Service 1 -->\n"
				+ "    <service id='1'>\n"
				+ "        <!-- This comment will be removed -->\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <!-- Service 1 with different configuration -->\n"
				+ "    <service id='1'>\n"
				+ "        <!-- Changed value -->\n"
				+ "        <parameter>other value</parameter>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "    <!-- End of configuration file -->\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <!-- Service 1 with different configuration -->\n"
				+ "    <service id='1'>\n"
				+ "        <!-- Changed value -->\n"
				+ "        <parameter>other value</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "    <!-- End of configuration file -->\n"
				+ "</config>";
		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);
	}

	@Test
	public void attributes() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='1' parameter='parameter' parameter2='parameter2'/>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <service id='1' parameter='other value' parameter3='parameter3'/>\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <service id='1' parameter='other value' parameter2='parameter2' parameter3='parameter3'/>\n"
				+ "</config>";
		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);
	}

	@Test
	public void remove() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "    <service id='2' combine.self='REMOVE'/>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <service id='1' combine.self='REMOVE'/>\n"
				+ "    <service id='2'/>\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <service id='2'/>\n"
				+ "</config>";

		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);
	}

	@Test
	public void override() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <service id='1' combine.self='OVERRIDE'>\n"
				+ "        <parameter>other value</parameter>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>other value</parameter>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "</config>";
		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);
	}

	@Test
	public void multipleChildren() throws SAXException, IOException, ParserConfigurationException,
			TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter9>parameter2</parameter9>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "    </service>\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <service id='1'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter9>parameter2</parameter9>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "</config>";
		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);
	}

	@Test
	public void defaults() throws SAXException, IOException, ParserConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='1' combine.self='DEFAULTS'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter9>parameter2</parameter9>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "    <service id='2' combine.self='DEFAULTS'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <service id='2'>\n"
				+ "    </service>\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <service id='2'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "</config>";
		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);
	}

	@Test
	public void overridable() throws SAXException, IOException, ParserConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='id1' combine.self='OVERRIDABLE'>\n"
				+ "        <test/>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "</config>";
		String dominant2 = "\n"
				+ "<config>\n"
				+ "    <service id='id1'/>\n"
				+ "</config>";
		String dominant3 = "\n"
				+ "<config>\n"
				+ "    <service id='id2'/>\n"
				+ "</config>";
		String result3 = "\n"
				+ "<config>\n"
				+ "    <service id='id1' combine.self='OVERRIDABLE'>\n"
				+ "        <test/>\n"
				+ "    </service>\n"
				+ "    <service id='id2'/>\n"
				+ "</config>";

		assertXMLIdentical(new Diff(recessive, combine(recessive, dominant)), true);
		assertXMLIdentical(new Diff(dominant2, combine(recessive, dominant2)), true);
		assertXMLIdentical(new Diff(result3, combine(recessive, dominant3)), true);
		assertXMLIdentical(new Diff(result3, combine(recessive, dominant, dominant3)), true);
	}

	@Test
	public void overridableByTag() throws SAXException, IOException, ParserConfigurationException,
			TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='id1' combine.self='OVERRIDABLE_BY_TAG'>\n"
				+ "        <test/>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "</config>";
		String dominant2 = "\n"
				+ "<config>\n"
				+ "    <service id='id1'/>\n"
				+ "</config>";
		String dominant3 = "\n"
				+ "<config>\n"
				+ "    <service id='id2'/>\n"
				+ "</config>";

		assertXMLIdentical(new Diff(recessive, combine(recessive, dominant)), true);
		assertXMLIdentical(new Diff(dominant2, combine(recessive, dominant2)), true);
		assertXMLIdentical(new Diff(dominant3, combine(recessive, dominant3)), true);
	}

	@Test
	public void subnodes() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<outer>\n"
				+ "  <inner>\n"
				+ "    content\n"
				+ "  </inner>\n"
				+ "  <inner2>\n"
				+ "    content2\n"
				+ "  </inner2>\n"
				+ "</outer>";
		String dominant = "\n"
				+ "<outer>\n"
				+ "  <inner>\n"
				+ "    content3\n"
				+ "  </inner>\n"
				+ "</outer>";
		String result = "\n"
				+ "<outer>\n"
				+ "  <inner>\n"
				+ "    content3\n"
				+ "  </inner>\n"
				+ "  <inner2>\n"
				+ "    content2\n"
				+ "  </inner2>\n"
				+ "</outer>";
		assertXMLIdentical(new Diff(result, combine(recessive, dominant)), true);

		String dominant2 = "\n"
				+ "<outer combine.children='APPEND'>\n"
				+ "  <inner>\n"
				+ "    content3\n"
				+ "  </inner>\n"
				+ "</outer>";
		String result2 = "\n"
				+ "<outer combine.children='APPEND'>\n"
				+ "  <inner>\n"
				+ "    content\n"
				+ "  </inner>\n"
				+ "  <inner2>\n"
				+ "    content2\n"
				+ "  </inner2>\n"
				+ "  <inner>\n"
				+ "    content3\n"
				+ "  </inner>\n"
				+ "</outer>";
		assertXMLIdentical(new Diff(result2, combine(recessive, dominant2)), true);

		String dominant3 = "\n"
				+ "<outer combine.self='OVERRIDE'>\n"
				+ "  <inner>\n"
				+ "    content3\n"
				+ "  </inner>\n"
				+ "</outer>";
		String result3 = "\n"
				+ "<outer>\n"
				+ "  <inner>\n"
				+ "    content3\n"
				+ "  </inner>\n"
				+ "</outer>";

		assertXMLIdentical(new Diff(result3, combine(recessive, dominant3)), true);
	}

	@Test
	public void threeDocuments() throws SAXException, IOException, ParserConfigurationException,
			TransformerConfigurationException, TransformerException {
		String recessive = "\n"
				+ "<config>\n"
				+ "    <service id='1' combine.self='DEFAULTS'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "    </service>\n"
				+ "    <service id='2' combine.self='DEFAULTS'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "    </service>\n"
				+ "    <service id='3'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "    </service>\n"
				+ "</config>";
		String middle = "\n"
				+ "<config>\n"
				+ "    <service id='1' combine.self='DEFAULTS'>\n"
				+ "        <parameter3>parameter3</parameter3>\n"
				+ "    </service>\n"
				+ "    <service id='2' combine.self='DEFAULTS'>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "    <service id='3' combine.self='DEFAULTS'>\n"
				+ "        <parameter2>parameter</parameter2>\n"
				+ "    </service>\n"
				+ "</config>";
		String dominant = "\n"
				+ "<config>\n"
				+ "    <service id='2'>\n"
				+ "    </service>\n"
				+ "</config>";
		String result = "\n"
				+ "<config>\n"
				+ "    <service id='2'>\n"
				+ "        <parameter>parameter</parameter>\n"
				+ "        <parameter2>parameter2</parameter2>\n"
				+ "    </service>\n"
				+ "</config>";
		assertXMLIdentical(new Diff(result, combine(recessive, middle, dominant)), true);
	}

	private static String combine(String... inputs) throws IOException,
			ParserConfigurationException, SAXException, TransformerConfigurationException,
			TransformerException {
		XmlCombiner combiner = new XmlCombiner();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		for (String input : inputs) {
			Document document = builder.parse(new ByteArrayInputStream(input.getBytes("UTF-8")));
			combiner.combine(document);
		}
		Document result = combiner.buildDocument();

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(result), new StreamResult(writer));
		return writer.toString();
	}
}
