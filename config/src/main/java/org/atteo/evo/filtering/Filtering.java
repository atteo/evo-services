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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Properties filtering engine.
 */
public class Filtering {
	private final static Logger logger = LoggerFactory.getLogger(Filtering.class);

	private final static class CircularCheckResolver implements PropertyResolver {
		private Set<String> inProgress = new HashSet<String>();
		private PropertyResolver resolver;

		private CircularCheckResolver(PropertyResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public String resolveProperty(String name, PropertyResolver ignore) throws PropertyNotFoundException {
			if (inProgress.contains(name)) {
				throw new CircularPropertyResolutionException(name);
			}
			logger.debug("Resolving property: " + name);
			inProgress.add(name);

			String value = resolver.resolveProperty(name, this);

			inProgress.remove(name);

			return value;
		}
	}

	public static final class Part {
		private String value;
		private boolean property;

		public Part(String value, boolean property) {
			this.value = value;
			this.property = property;
		}

		public String getValue() {
			return value;
		}

		public boolean isProperty() {
			return property;
		}
	}

	public static String getProperty(String value, PropertyResolver resolver) throws PropertyNotFoundException {
		// optimization: we don't need another one
		if (resolver instanceof CircularCheckResolver) {
			return resolver.resolveProperty(value, null);
		}
		return new CircularCheckResolver(resolver).resolveProperty(value, resolver);
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the value using given property resolver.
	 * @param value the value to filter the properties into
	 * @param propertyResolver resolver for the property values
	 * @return filtered value
	 * @throws PropertyNotFoundException when some property cannot be found
	 */
	public static String filter(String value, PropertyResolver propertyResolver)
			throws PropertyNotFoundException {
		List<Part> parts = splitIntoParts(value);
		StringBuilder result = new StringBuilder();

		for (Part part : parts) {
			if (part.isProperty()) {
				String propertyValue = getProperty(part.getValue(), propertyResolver);
				if (propertyValue == null) {
					throw new PropertyNotFoundException(part.getValue());
					//result.append(value.subSequence(position, endposition + 1));
					//break;
				}
				result.append(propertyValue);
			} else {
				result.append(part.getValue());
			}
		}
		return result.toString();
	}
	
	/**
	 * Find <code>${name}</code> placeholders in the given String.
	 * @param value the value to filter the properties into
	 * @return list of parts
	 */
	public static List<Part> splitIntoParts(String value) {
		List<Part> parts = new ArrayList<Part>();
		int index = 0;

		while (true) {
			int position = value.indexOf("${", index);
			if (position == -1) {
				break;
			}
			if (index != position) {
				parts.add(new Part(value.substring(index, position), false));
			}

			// find '${' and '}' pair, correctly handle nested pairs
			boolean lastDollar = false;
			int count = 1;
			int countBrace = 0;
			int endposition;
			for (endposition = position + 2; endposition < value.length(); endposition++) {
				if (value.charAt(endposition) == '$') {
					lastDollar = true;
					continue;
				}
				if (value.charAt(endposition) == '{') {
					if (lastDollar) {
						count++;
					} else {
						countBrace++;
					}
				} else if (value.charAt(endposition) == '}') {
					if (countBrace > 0) {
						countBrace--;
					} else {
						count--;
						if (count == 0) {
							break;
						}
					}
				}
				lastDollar = false;
			}

			if (count > 0) {
				index = position;
				break;
			}
			String propertyName = value.substring(position + 2, endposition);
			index = endposition + 1;

			parts.add(new Part(propertyName, true));
		}

		if (index != value.length()) {
			parts.add(new Part(value.substring(index), false));
		}
		return parts;
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the value using given properties.
	 * @param value the value to filter the properties into
	 * @param properties properties to filter into the value
	 * @return filtered value
	 * @throws PropertyNotFoundException when some property cannot be found
	 */
	public static String filter(String value, final Properties properties)
			throws PropertyNotFoundException {

		return filter(value, new PropertiesPropertyResolver(properties));
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the XML element.
	 * 
	 * <p>
	 * The structure of the XML document is not changed. Each attribute and element text is filtered
	 * separately.
	 * </p>
	 * @param element
	 * @param propertyResolver
	 * @throws PropertyNotFoundException 
	 */
	public static void filter(Element element, PropertyResolver propertyResolver)
			throws PropertyNotFoundException {
		XmlFiltering filtering = new XmlFiltering(propertyResolver);
		filtering.filterElement(element);
	}

	/**
	 * Filter <code>${name}</code> placeholders found within the XML element. 
	 * 
	 * @see #filter(Element, PropertyResolver)  
	 */
	public static void filter(Element element, Properties properties)
			throws PropertyNotFoundException {
		filter(element, new PropertiesPropertyResolver(properties));
	}

	private static class XmlFiltering {
		private PropertyResolver propertyResolver;

		private XmlFiltering(PropertyResolver propertyResolver) {
			this.propertyResolver = propertyResolver;
		}

		private void filterElement(Element element) throws PropertyNotFoundException {
			NamedNodeMap attributes = element.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node node = attributes.item(i);
				filterAttribute((Attr) node);
			}

			NodeList nodes = element.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				switch (node.getNodeType()) {
					case Node.ELEMENT_NODE:
						filterElement((Element) node);
						break;
					case Node.TEXT_NODE:
						filterText((Text) node);
						break;
				}
			}
		}

		private void filterAttribute(Attr attribute) throws PropertyNotFoundException {
			attribute.setValue(filterString(attribute.getValue()));
		}

		private void filterText(Text text) throws PropertyNotFoundException {
			text.setTextContent(filterString(text.getTextContent()));
		}

		private String filterString(String value) throws PropertyNotFoundException {
			return Filtering.filter(value, propertyResolver);
		}
	}
}
