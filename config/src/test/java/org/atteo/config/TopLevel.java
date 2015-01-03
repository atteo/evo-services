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

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.xmlcombiner.CombineChildren;

@XmlRootElement
public class TopLevel extends AbstractConfigurable {
	@Valid
	@XmlElementRef
	List<Entry> entries;

	@Valid
	@XmlCombine(children = CombineChildren.APPEND)
	@XmlElementWrapper(name = "append")
	@XmlElementRef
	List<Entry> append = new ArrayList<>();

	@XmlElement
	MiddleLevel middle;

	@XmlAttribute
	@XmlIDREF
	Entry specialEntry;

	@XmlAttribute
	@XmlIDREF
	MiddleLevel specialMiddle;
}
