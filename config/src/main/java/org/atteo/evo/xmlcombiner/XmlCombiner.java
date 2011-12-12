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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;

import org.atteo.evo.config.Combine;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Combines two XML DOM trees.
 */
public class XmlCombiner {
	private static class Key {
		private String name;
		private String id;

		public static Key fromElement(Element element) {
			if (element.hasAttribute("id")) {
				return new Key(element.getTagName(), element.getAttribute("id"));
			}

			return new Key(element.getTagName(), null);
		}

		public Key(String name, String id) {
			this.name = name;
			this.id = id;
		}

		@Override
		public int hashCode() {
			int hash = 1;
			if (name != null) {
				hash += name.hashCode();
			}
			if (id != null) {
				hash = hash * 37 + id.hashCode();
			}
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Key other = (Key) obj;
			if ((name == null) ? (other.getName() != null) : !name.equals(other.getName())) {
				return false;
			}
			if ((id == null) ? (other.getId() != null) : !id.equals(other.getId())) {
				return false;
			}
			return true;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static Document combine(DocumentBuilder documentBuilder, Document parent,
			Document child) {
		Document resultDocument = documentBuilder.newDocument();
		Element result = combine(resultDocument, parent.getDocumentElement(),
				child.getDocumentElement());

		resultDocument.appendChild(result);

		return resultDocument;
	}

	public static Element combine(Document document, @Nullable Element parent,
			@Nullable Element child) {
		Combine combine = getCombineAttribute(child);
		if (combine == null) {
			combine = getCombineAttribute(parent);
			if (combine == null) {
				combine = Combine.MERGE;
			}
		}

		if (combine == Combine.DEFAULTS) {
			if (child == null) {
				return null;
			}
			combine = Combine.MERGE;
		}

		if (child == null) {
			child = parent;
			parent = null;
		}

		if (combine == Combine.REMOVE) {
			return null;
		}

		if (combine == Combine.OVERRIDE) {
			return copyRecursively(document, child);
		}

		if (combine == Combine.APPEND) {
			Element result;
			if (parent != null) {
				result = copyRecursively(document, parent);
				appendRecursively(document, child, result);
			} else {
				result = copyRecursively(document, child);
			}
			return result;
		}

		assert(combine == Combine.MERGE);

		Element result = document.createElement(child.getTagName());

		copyAttributes(document, parent, result);
		copyAttributes(document, child, result);
		
		// TODO: insert comments with next node
		Map<Key, List<Element>> parentMap = mapSubElements(parent);
		Map<Key, List<Element>> childMap = mapSubElements(child);

		if (parent != null && !parentMap.isEmpty()) {
			// Execute only if there is at least one subelement in parent
			NodeList nodes = parent.getChildNodes();
			boolean lastIncluded = false;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element element = (Element) node;
					Key key = Key.fromElement(element);
					if (childMap.containsKey(key) && childMap.get(key).size() == 1
							&& parentMap.get(key).size() == 1) {
						lastIncluded = false;
						continue;
					}
					Element combined = combine(document, element, null);
					if (combined != null) {
						result.appendChild(combined);
						lastIncluded = true;
					} else {
						lastIncluded = false;
					}
				} else if (node instanceof Text && !lastIncluded) {
					Text text = (Text) node;
					if (! text.getTextContent().trim().isEmpty()) {
						result.appendChild(document.importNode(node, true));
						lastIncluded = true;
					}
				} else  {
					result.appendChild(document.importNode(node, true));
					lastIncluded = true;
				}
			}
		}

		NodeList nodes = child.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			boolean lastIncluded = true;
			if (node instanceof Element) {
				Element element = (Element) node;
				Key key = Key.fromElement(element);
				Element combined;
				if (parentMap.containsKey(key) && childMap.get(key).size() == 1
						&& parentMap.get(key).size() == 1) {
					combined = combine(document, parentMap.get(key).get(0), element);
				} else {
					combined = combine(document, null, element);
				}
				if (combined != null) {
					result.appendChild(combined);
					lastIncluded = true;
				} else {
					lastIncluded = false;
				}
			} else if (node instanceof Text && !lastIncluded) {
				Text text = (Text) node;
				if (! text.getTextContent().trim().isEmpty()) {
					result.appendChild(document.importNode(node, true));
					lastIncluded = true;
				}
			}  else {
				result.appendChild(document.importNode(node, true));
				lastIncluded = true;
			}
		}

		return result;
	}

	private static Element copyRecursively(Document document, Element element) {
		Element result = (Element) document.importNode(element, false);
		appendRecursively(document, element, result);

		return result;
	}

	private static void appendRecursively(Document document, Element element, Element result) {
		copyAttributes(document, element, result);
		NodeList nodes = element.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				Element child = (Element) node;
				Element combined = combine(document, null, child);
				if (combined != null) {
					result.appendChild(combined);
				}
			} else {
				result.appendChild(document.importNode(node, true));
			}
		}
	}

	private static void copyAttributes(Document document,@Nullable Element element,
			Element result) {
		if (element == null) {
			return;
		}
		NamedNodeMap attributes = element.getAttributes();
		for (int i=0; i<attributes.getLength(); i++) {
			Attr attribute = (Attr) attributes.item(i);
			// contrary to the documentation setAttributeNodeNS does not override attribute value
			result.removeAttributeNS(attribute.getNamespaceURI(), attribute.getName());
			result.setAttributeNodeNS((Attr)document.importNode(attribute, true));
		}
	}

	/**
	 * Creates a map of (tagName, id) to list of elements.
	 * @param element root element of the tree to map
	 * @return calculated map
	 */
	@SuppressWarnings("unchecked")
	private static Map<Key, List<Element>> mapSubElements(@Nullable Element element) {
			Map<Key, List<Element>> map = new HashMap<Key, List<Element>>();
			if (element == null) {
				return map;
			}
			NodeList nodes = element.getChildNodes();
			for (int i=0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element subelement = (Element) node;
					Attr idNode = subelement.getAttributeNode("id");
					String id = null;
					if (idNode != null) {
						id = idNode.getValue();
					}
					Key key = new Key(subelement.getTagName(), id);
					List<Element> list = map.get(key);
					if (list == null) {
						list = new LinkedList<Element>();
						map.put(key, list);
					}
					list.add(subelement);
				}
			}
			return map;
	}

	private static Combine getCombineAttribute(@Nullable Element element) {
		Combine combine = null;
		if (element == null) {
			return null;
		}
		Attr combineAttribute = element.getAttributeNode("combine");
		if (combineAttribute != null) {
			try {
				combine = Combine.valueOf(combineAttribute.getValue());
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("The attribute 'combine' of element '"
						+ element.getTagName()	+ "' has invalid value '"
						+ combineAttribute.getValue(), e);
			}
		}
		return combine;
	}
}
