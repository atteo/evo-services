/*
 * Copyright 2014 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atteo.config;

import java.io.StringReader;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class InterfacesTest {
	@Test
	public void shouldSupportInterfaces() throws JAXBException {
		JAXBContext context = JAXBContextFactory.createContext(new Class<?>[] { People.class, Person.class,
			John.class}, Collections.emptyMap());

		String content = ""
				+ "<people>"
				+ "    <john id='first'/>"
				+ "    <john id='second'>"
				+ "        <parent>first</parent>"
				+ "    </john>"
				+ "</people>";

		People result = (People) context.createUnmarshaller().unmarshal(new StringReader(content));

		assertEquals(2, result.getPeople().size());
	}
}
