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

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.ListMultimap;

/**
 * Combines two or more XML DOM trees.
 *
 * <p>
 * The merging algorithm is as follows:<br/>
 * First direct subelements of selected node are examined.
 * The elements from both trees with matching tag names and the value of 'id' attributes are paired.
 * Based on selected behavior the content of the paired elements is then merged.
 * Finally the paired elements are recursively combined. Any not paired elements are appended.
 * </p>
 * <p>
 * You can control merging behavior using {@link CombineSelf 'combine.self'}
 * and {@link CombineChildren 'combine.children'} attributes.
 * </p>
 * <p>
 * The merging algorithm was inspired by similar functionality in Plexus Utils.
 * </p>
 *
 * @see <a href="http://www.sonatype.com/people/2011/01/maven-how-to-merging-plugin-configuration-in-complex-projects/">merging in Maven</a>
 * @see <a href="http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/xml/Xpp3DomUtils.html">Plexus utils implementation of merging</a>
 */
public class XmlCombiner {

	private final DocumentBuilder documentBuilder;
	private final Document document;

	/**
	 * Creates XML combiner using default {@link DocumentBuilder}.
	 * @throws ParserConfigurationException when {@link DocumentBuilder} creation fails
	 */
	public XmlCombiner() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		documentBuilder = factory.newDocumentBuilder();
		document = documentBuilder.newDocument();
	}

	public XmlCombiner(DocumentBuilder documentBuilder) {
		this.documentBuilder = documentBuilder;
		document = documentBuilder.newDocument();
	}

	/**
	 * Combine given document.
	 * @param document document to combine
	 */
	public void combine(Document document) {
		combine(document.getDocumentElement());
	}

	/**
	 * Combine given element.
	 * @param element element to combine
	 */
	public void combine(Element element) {
		Element parent = document.getDocumentElement();
		if (parent != null) {
			document.removeChild(parent);
		}
		Context result = combine(Context.fromElement(parent), Context.fromElement(element));
		result.addAsChildTo(document);
	}

	public Document buildDocument() {
		filterOutDefaults(Context.fromElement(document.getDocumentElement()));
		return document;
	}

	private Context combine(Context recessive, Context dominant) {
		CombineSelf dominantCombineSelf = getCombineSelf(dominant.getElement());
		CombineSelf recessiveCombineSelf = getCombineSelf(recessive.getElement());

		if (dominantCombineSelf == CombineSelf.REMOVE) {
			return null;
		} else if (dominantCombineSelf == CombineSelf.OVERRIDE
				|| (recessiveCombineSelf == CombineSelf.OVERRIDABLE)) {
			Context result = copyRecursively(dominant);
			result.getElement().removeAttribute(CombineSelf.ATTRIBUTE_NAME);
			return result;
		}

		CombineChildren combineChildren = getCombineChildren(dominant.getElement());
		if (combineChildren == null) {
			combineChildren = getCombineChildren(recessive.getElement());
			if (combineChildren == null) {
				combineChildren = CombineChildren.MERGE;
			}
		}

		if (combineChildren == CombineChildren.APPEND) {
			if (recessive.getElement() != null) {
				removeWhitespaceTail(recessive.getElement());
				appendRecursively(dominant, recessive);
				return recessive;
			} else {
				return copyRecursively(dominant);
			}
		}

		Element resultElement = document.createElement(dominant.getElement().getTagName());

		copyAttributes(recessive.getElement(), resultElement);
		copyAttributes(dominant.getElement(), resultElement);

		// when dominant combineSelf is null or DEFAULTS use combineSelf from recessive
		CombineSelf combineSelf = dominantCombineSelf;
		if ((combineSelf == null && recessiveCombineSelf != CombineSelf.DEFAULTS)) {
				//|| (combineSelf == CombineSelf.DEFAULTS && recessive.getElement() != null)) {
			combineSelf = recessiveCombineSelf;
		}
		if (combineSelf != null) {
			resultElement.setAttribute(CombineSelf.ATTRIBUTE_NAME, combineSelf.name());
		} else {
			resultElement.removeAttribute(CombineSelf.ATTRIBUTE_NAME);
		}

		ListMultimap<Key, Context> recessiveContexts = recessive.mapChildContexts();
		ListMultimap<Key, Context> dominantContexts = dominant.mapChildContexts();

		Set<String> tagNamesInDominant = getTagNames(dominantContexts);

		// Execute only if there is at least one subelement in recessive
		if (!recessiveContexts.isEmpty()) {
			for (Entry<Key, Context> entry : recessiveContexts.entries()) {
				Key key = entry.getKey();
				Context recessiveContext = entry.getValue();

				if (key == Key.BEFORE_END) {
					continue;
				}

				if (getCombineSelf(recessiveContext.getElement()) == CombineSelf.OVERRIDABLE_BY_TAG) {
					if (!tagNamesInDominant.contains(key.getName())) {
						recessiveContext.addAsChildTo(resultElement);
					}
					continue;
				}

				if (dominantContexts.get(key).size() == 1 && recessiveContexts.get(key).size() == 1) {
					Context dominantContext = dominantContexts.get(key).iterator().next();

					Context combined = combine(recessiveContext, dominantContext);
					if (combined != null) {
						combined.addAsChildTo(resultElement);
					}
				} else {
					recessiveContext.addAsChildTo(resultElement);
				}
			}
		}

		for (Entry<Key, Context> entry : dominantContexts.entries()) {
			Key key = entry.getKey();
			Context dominantContext = entry.getValue();

			if (key == Key.BEFORE_END) {
				dominantContext.addAsChildTo(resultElement, document);
				// break? this should be the last anyway...
				continue;
			}
			List<Context> associatedRecessives = recessiveContexts.get(key);
			if (dominantContexts.get(key).size() == 1 && associatedRecessives.size() == 1
					&& getCombineSelf(associatedRecessives.get(0).getElement()) != CombineSelf.OVERRIDABLE_BY_TAG) {
				// already added
			} else {
				Context combined = combine(Context.fromElement(null), dominantContext);
				if (combined != null) {
					combined.addAsChildTo(resultElement);
				}
			}
		}

		Context result = new Context();
		result.setElement(resultElement);
		appendNeighbours(dominant, result);

		return result;
	}

	/**
	 * Copy element recursively.
	 * @param context context to copy, it is assumed it is from unrelated document
	 * @return copied element in current document
	 */
	private Context copyRecursively(Context context) {
		Context copy = new Context();

		appendNeighbours(context, copy);

		Element element = (Element) document.importNode(context.getElement(), false);
		copy.setElement(element);

		appendRecursively(context, copy);

		return copy;
	}

	/**
	 * Append neighbours from source to destination
	 * @param source source element, it is assumed it is from unrelated document
	 * @param destination destination element
	 */
	private void appendNeighbours(Context source, Context destination) {
		for (Node neighbour : source.getNeighbours()) {
			destination.addNeighbour(document.importNode(neighbour, true));
		}
	}

	/**
	 * Appends all attributes and subelements from source element do destination element.
	 * @param source source element, it is assumed it is from unrelated document
	 * @param destination destination element
	 */
	private void appendRecursively(Context source, Context destination) {
		copyAttributes(source.getElement(), destination.getElement());

		List<Context> contexts = source.groupChildContexts();

		for (Context context : contexts) {
			if (context.getElement() == null) {
				context.addAsChildTo(destination.getElement(), document);
				continue;
			}
			Context combined = combine(Context.fromElement(null), context);
			if (combined != null) {
				combined.addAsChildTo(destination.getElement());
			}
		}
	}

	/**
	 * Copies attributes from one {@link Element} to the other.
	 * @param source source element
	 * @param destination destination element
	 */
	private void copyAttributes(@Nullable Element source, Element destination) {
		if (source == null) {
			return;
		}
		NamedNodeMap attributes = source.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attribute = (Attr) attributes.item(i);
			Attr destAttribute = destination.getAttributeNodeNS(attribute.getNamespaceURI(), attribute.getName());

			if (destAttribute == null) {
				destination.setAttributeNodeNS((Attr) document.importNode(attribute, true));
			} else {
				destAttribute.setValue(attribute.getValue());
			}
		}
	}

	private static CombineSelf getCombineSelf(@Nullable Element element) {
		CombineSelf combine = null;
		if (element == null) {
			return null;
		}
		Attr combineAttribute = element.getAttributeNode(CombineSelf.ATTRIBUTE_NAME);
		if (combineAttribute != null) {
			try {
				combine = CombineSelf.valueOf(combineAttribute.getValue().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("The attribute 'combine' of element '"
						+ element.getTagName() + "' has invalid value '"
						+ combineAttribute.getValue(), e);
			}
		}
		return combine;
	}

	private static CombineChildren getCombineChildren(@Nullable Element element) {
		CombineChildren combine = null;
		if (element == null) {
			return null;
		}
		Attr combineAttribute = element.getAttributeNode(CombineChildren.ATTRIBUTE_NAME);
		if (combineAttribute != null) {
			try {
				combine = CombineChildren.valueOf(combineAttribute.getValue().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("The attribute 'combine' of element '"
						+ element.getTagName() + "' has invalid value '"
						+ combineAttribute.getValue(), e);
			}
		}
		return combine;
	}

	private static void removeWhitespaceTail(Element element) {
		NodeList list = element.getChildNodes();
		for (int i = list.getLength() - 1; i >= 0; i--) {
			Node node = list.item(i);
			if (node instanceof Element) {
				break;
			}
			element.removeChild(node);
		}
	}

	private static void filterOutDefaults(Context context) {
		Element element = context.getElement();
		List<Context> childContexts = context.groupChildContexts();

		for (Context childContext : childContexts) {
			if (childContext.getElement() == null) {
				continue;
			}
			CombineSelf combineSelf = getCombineSelf(childContext.getElement());
			if (combineSelf == CombineSelf.DEFAULTS) {
				for (Node neighbour : childContext.getNeighbours()) {
					element.removeChild(neighbour);
				}
				element.removeChild(childContext.getElement());
			} else {
				filterOutDefaults(childContext);
			}
		}
	}

	private static Set<String> getTagNames(ListMultimap<Key, Context> dominantContexts) {
		Set<String> names = new HashSet<>();
		for (Key key : dominantContexts.keys()) {
			names.add(key.getName());
		}
	
		return names;
	}
}
