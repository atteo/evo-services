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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.atteo.classindex.ClassFilter;
import org.atteo.classindex.ClassIndex;
import org.atteo.config.jaxb.FilteringAnnotationReader;
import org.atteo.config.jaxb.JaxbBindings;
import org.atteo.filtering.CompoundPropertyResolver;
import org.atteo.filtering.Filtering;
import org.atteo.filtering.PropertiesPropertyResolver;
import org.atteo.filtering.PropertyFilter;
import org.atteo.filtering.PropertyNotFoundException;
import org.atteo.filtering.PropertyResolver;
import org.atteo.xmlcombiner.CombineChildren;
import org.atteo.xmlcombiner.CombineSelf;
import org.atteo.xmlcombiner.XmlCombiner;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.JAXBHelper;
import org.eclipse.persistence.jaxb.javamodel.reflection.AnnotationHelper;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.collect.Iterables;

/**
 * Generic configuration facility based on JAXB.
 *
 * <h3>Overview</h3>
 * <p>
 * Atteo Config opens one or more XML files, merges their content, filters them
 * and then converts into the tree of objects using JAXB.
 * </p>
 * <h3>Defining XML schema</h3>
 * <p>
 * To use Atteo Config you need to define the schema for your configuration file.
 * This is achieved by creating a number of classes which extend {@link Configurable} abstract class
 * and annotating them with
 * <a href="http://jaxb.java.net/2.2.6/docs/ch03.html#annotating-your-classes">JAXB annotations</a>.
 * Let's start by defining classes for generic service and some specific database service:
 * <pre>
 * {@code
 * abstract class Service extends Configurable {
 *   public abstract void start();
 * }
 *
 *. @XmlRootElement(name = "database")
 * class Database extends Service {
 *.  @XmlElement
 *   private String url;
 *
 *   public void start() {
 *     System.out.println("Connecting to database: " + url);
 *   }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * Also let's create the class which will define root of the configuration schema. Here the common idiom
 * is to create field of type {@link List} annotated with {@link XmlElementRef}. Using this JAXB
 * will be able to unmarshal any subclass of list element type. In this way the schema is open-ended,
 * allowing anyone to implement our Service. There is no need to list all the implementations:
 * <pre>
 * {@code
 *. @XmlRootElement(name = "config")
 *  class Config extends Configurable {
 *.    @XmlElementRef
 *.    @XmlElementWrapper(name = "services")
 *.    @Valid
 *     private List<Service> services;
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * The above schema will match the following XML:
 *
 * <pre>
 * {@code
 * <config>
 *   <services>
 *     <database>
 *       <url>jdbc:h2:file:/data/sample</url>
 *     </database>
 *     <database>
 *       <url>jdbc:h2:tcp://localhost/~/test</url>
 *     </database>
 *   </services>
 * </config>
 * }
 * </pre>
 * </p>
 * </p>
 *
 * <h3>Reading configuration files</h3>
 *
 * <pre>
 *    Configuration configuration = new Configuration();
 *    configuration.combine("first.xml");
 *    configuration.combine("second.xml");
 *    configuration.filter(properties);
 *    Root root = configuration.read(Root.class);
 * </pre>
 * </p>
 * <p>
 * The following actions will be performed:
 * <ul>
 * <li>{@link JAXBContext} will be created for all the classes extending {@link Configurable},
 * those classes are indexed at compile-time using {@link ClassIndex} facility,</li>
 * <li>provided XML files will be parsed and combined using {@link XmlCombiner} facility,</li>
 * <li>any property references in the form of <code>${name}</code> will be substituted
 * with the value using registered {@link PropertyResolver}, see {@link Filtering} for details,</li>
 * <li>the result will be unmarshalled using {@link Unmarshaller JAXB} into provided root class,</li>
 * <li>finally the unmarshalled object tree will be validated using JSR 303
 * - {@link Validation Bean Validation framework}.</li>
 * </ul>
 * </p>
 */
public class Configuration {
	private JAXBContext context;
	private Binder<Node> binder;
	private final Iterable<Class<? extends Configurable>> klasses;
	private DocumentBuilder builder;
	private Document document;
	private PropertyFilter propertyFilter;
	//private RuntimeAnnotationReader annotationReader = new RuntimeInlineAnnotationReader();

	/**
	 * Create Configuration by discovering all {@link Configurable}s.
	 *
	 * <p>
	 * Uses {@link ClassIndex#getSubclasses(Class)} to get list of top-level classes implementing {@link Configurable}
	 * interface.
	 * </p>
	 */
	public Configuration() {
		this(ClassFilter.only().topLevel().from(ClassIndex.getSubclasses(Configurable.class)));
	}

	/**
	 * Create Configuration by manually specifying all {@link Configurable}s.
	 * @param klasses list of {@link Configurable} classes.
	 * @throws JAXBException when JAXB context creation fails
	 */
	public Configuration(Iterable<Class<? extends Configurable>> klasses) {
		this.klasses = klasses;
		propertyFilter = Filtering.getFilter(new PropertyResolver() {
			@Override
			public String resolveProperty(String name, PropertyFilter filter) throws PropertyNotFoundException {
				throw new PropertyNotFoundException(name);
			}
		});
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
			// register null error handler, fatal errors will be reported with exception anyway
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception) throws SAXException {
				}
				@Override
				public void error(SAXParseException exception) throws SAXException {
				}
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
				}
			});
			context = JAXBContextFactory.createContext(Iterables.toArray(klasses, Class.class), Collections.emptyMap());
			binder = context.createBinder();
			// JAXB Moxy does not allow to set resolver on binder
//			binder.setProperty(UnmarshallerProperties.ID_RESOLVER, new ScopedIdResolver());
			binder.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(ValidationEvent event) {
					return true;
				}
			});
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Cannot configure XML parser", e);
		} catch (JAXBException e) {
			throw new RuntimeException("Cannot configure unmarshaller", e);
		}
	}

	/**
	 * Generate an XSD schema for the configuration file.
	 * @param filename file to store the schema to
	 * @throws IOException when IO error occurs
	 */
	public void generateSchema(final File filename) throws IOException {
		context.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName)
					throws IOException {
				// We should just call:
				//     return new StreamResult(filename);
				// but this does not work due to the https://java.net/jira/browse/JAXB-974
				try {
					SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
					TransformerHandler transformer = factory.newTransformerHandler();
					transformer.setResult(new StreamResult(new FileOutputStream(filename)));
					SAXResult saxResult = new SAXResult(transformer);
					saxResult.setSystemId("dummy");
					return saxResult;
				} catch (TransformerConfigurationException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	/**
	 * Filter {@code ${name}} placeholders using given properties.
	 * <p>
	 * This method wraps given properties into {@link PropertiesPropertyResolver}
	 * and calls {@link #filter(PropertyResolver)}.
	 * </p>
	 * @param properties properties to filter into configuration files
	 */
	public void filter(Properties properties) throws IncorrectConfigurationException {
		filter(new PropertiesPropertyResolver(properties));
	}

	/**
	 * Filter {@code ${name}} placeholders using values from given {@link PropertyResolver}.
	 *
	 * @param resolver property resolver used for filtering the configuration files
	 *
	 * @see CompoundPropertyResolver
	 */
	public void filter(PropertyResolver resolver) throws IncorrectConfigurationException {
		propertyFilter = Filtering.getFilter(resolver);
		if (document.getDocumentElement() == null) {
			return;
		}
		try {
			propertyFilter.filter(document.getDocumentElement());
		} catch (PropertyNotFoundException e) {
			throw new IncorrectConfigurationException("Cannot resolve configuration properties: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Parse an XML file and combine it with the currently stored DOM tree.
	 * @param stream stream with the XML file
	 * @throws IncorrectConfigurationException when configuration file is invalid
	 * @throws IOException when the stream cannot be read
	 */
	public void combine(InputStream stream) throws IncorrectConfigurationException, IOException {
		Document parentDocument = document;

		try {
			document = builder.parse(stream);

			// Unmarshall the parent document to assign combine attributes annotated on classes
			Element root = parentDocument.getDocumentElement();
			if (root != null) {
				binder.unmarshal(root);
				JaxbBindings.iterate(root, binder, new CombineAssigner());

				// Combine with parent
				XmlCombiner combiner = new XmlCombiner(builder);
				combiner.combine(parentDocument);
				combiner.combine(document);
				document = combiner.buildDocument();
			}
		} catch (UnmarshalException e) {
			if (e.getLinkedException() != null) {
				throw new IncorrectConfigurationException("Cannot parse configuration file: "
						+ e.getLinkedException().getMessage(), e.getLinkedException());
			} else {
				throw new RuntimeException("Cannot parse configuration file", e);
			}
		} catch (JAXBException e) {
			throw new IncorrectConfigurationException("Unmarshall error: " + e.getMessage(), e);
		} catch (SAXException e) {
			throw new IncorrectConfigurationException("Parse error: " + e.getMessage(), e);
		}
	}

	/**
	 * Unmarshals stored configuration DOM tree as object of the given class.
	 * @param rootClass the class to which unmarshal the DOM tree
	 * @param <T> type of the rootClass
	 * @return unmarshalled class tree, or null if no streams were provided
	 * @throws IncorrectConfigurationException if configuration is incorrect
	 */
	public <T extends Configurable> T read(Class<T> rootClass) throws IncorrectConfigurationException {
		if (document.getDocumentElement() == null) {
			return null;
		}
		T result;
		final StringBuilder errors = new StringBuilder();
		try {
			Map<String, Object> properties = new HashMap<>();
			AnnotationHelper helper = new FilteringAnnotationReader(propertyFilter);
			properties.put(JAXBContextProperties.ANNOTATION_HELPER, helper);
			context = JAXBContextFactory.createContext(Iterables.toArray(klasses, Class.class), properties);
			binder = context.createBinder();
			// JAXB Moxy does not allow to set resolver on binder
//			binder.setProperty(UnmarshallerProperties.ID_RESOLVER, new ScopedIdResolver());
			binder.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(ValidationEvent event) {
					if (event.getLocator().getLineNumber() != -1) {
						errors.append("\n  At line ").append(event.getLocator().getLineNumber());
					} else if (event.getLocator().getNode() != null &&
					    event.getLocator().getNode().getParentNode() != null) {
						errors.append("\n  In <");
						errors.append(event.getLocator().getNode().getParentNode().getNodeName());
						errors.append(">");
					}

					errors.append(": ").append(event.getMessage());
					return false;
				}
			});
			result = rootClass.cast(binder.unmarshal(document.getDocumentElement()));
			JaxbBindings.iterate(document.getDocumentElement(), binder,
					new DefaultsSetter(context, propertyFilter));

			ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
			Validator validator = validatorFactory.getValidator();
			Set<ConstraintViolation<T>> violations = validator.validate(result);
			if (!violations.isEmpty()) {
				for (ConstraintViolation<T> violation : violations) {
					errors.append("  Error at:   ").append(violation.getPropertyPath()).append("\n")
							.append("    for value:   ").append(violation.getInvalidValue()).append("\n")
							.append("    with message:    ").append(violation.getMessage());
				}
				throw new IncorrectConfigurationException("Constraints violation:" + errors.toString());
			}
		} catch (UnmarshalException e) {
			if (e.getLinkedException() != null) {
				throw new IncorrectConfigurationException("Parse error: " + e.getLinkedException().getMessage(),
						e.getLinkedException());
			} else if (errors.length() > 0) {
				throw new IncorrectConfigurationException("Parse error: " + errors.toString(), e);
			} else {
				throw new IncorrectConfigurationException("Parse error:" + e.getMessage(), e);
			}
		} catch (JAXBException e) {
			throw new IncorrectConfigurationException("Cannot unmarshall configuration file", e);
		}

		return result;
	}

	/**
	 * Get root XML {@link Element} of the combined configuration file.
	 * @return root {@link Element}
	 */
	public Element getRootElement() {
		return document.getDocumentElement();
	}

	/**
	 * Combines several input XML documents and reads the configuration from it.
	 *
	 * <p>
	 * This is equivalent to executing {@link #combine(InputStream)} for each stream
	 * and then {@link #read(Class)} for rootClass.
	 * </p>
	 * @param rootClass the class to which unmarshal the DOM tree
	 * @param <T> type of the rootClass
	 * @param streams input streams with the configuration to combine
	 * @return unmarshalled class tree, or null if no streams were provided
	 * @throws IncorrectConfigurationException if configuration is incorrect
	 * @throws IOException when cannot access configuration files
	 */
	public <T extends Configurable> T read(Class<T> rootClass, InputStream... streams)
			throws IncorrectConfigurationException, IOException {
		for (InputStream stream : streams) {
			combine(stream);
		}
		return read(rootClass);
	}


	private static class CombineAssigner implements JaxbBindings.Runnable {
		/**
		 * Assigns {@link Configurable#combine} from the fields or class this objects unmarshals to.
		 * @param element DOM element
		 * @param object object into which DOM element was unmarshalled
		 * @param field field which holds unmarshalled object
		 */
		@Override
		public void run(Element element, Object object, Field field) {
			if (field != null) {
				setAttributesFromAnnotation(element, field.getAnnotation(XmlCombine.class));
			}

			if (object != null) {
				setAttributesFromAnnotation(element, object.getClass().getAnnotation(XmlCombine.class));
			}
		}

		private void setAttributesFromAnnotation(Element element, XmlCombine annotation) {
			if (annotation != null) {
				if (!element.hasAttribute(CombineSelf.ATTRIBUTE_NAME)) {
					CombineSelf combineSelf = annotation.self();
					if (combineSelf != null) {
						element.setAttribute(CombineSelf.ATTRIBUTE_NAME, combineSelf.name());
					}
				}
				if (!element.hasAttribute(CombineChildren.ATTRIBUTE_NAME)) {
					CombineChildren combineChildren = annotation.children();
					if (combineChildren != null) {
						element.setAttribute(CombineChildren.ATTRIBUTE_NAME, combineChildren.name());
					}
				}
			}
		}
	}

	private static class DefaultsSetter implements JaxbBindings.Runnable {
		private final JAXBContext context;
		private final PropertyFilter properties;

		public DefaultsSetter(JAXBContext context, PropertyFilter properties) {
			this.context = context;
			this.properties = properties;
		}

		@Override
		public void run(Element element, Object object, Field field) {
			Class<?> klass = object.getClass();

			while (klass != Object.class) {
				for (Field f : klass.getDeclaredFields()) {
					XmlDefaultValue defaultValue = f.getAnnotation(XmlDefaultValue.class);
					if (defaultValue != null) {
						if (f.getType().isPrimitive()) {
							throw new RuntimeException("@XmlDefaultValue cannot be specified on primitive type: "
									+ klass.getCanonicalName() + "." + f.getName());
						}

						boolean accessible = f.isAccessible();
						f.setAccessible(true);
						try {
							if (f.get(object) != null) {
								continue;
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}

						String value = defaultValue.value();
						try {
							value = properties.filter(value);
						} catch (PropertyNotFoundException e) {
							if (field != null) {
								throw new RuntimeException("Property not found for field '"
										+ field.getName() + "'", e);
							} else {
								throw new RuntimeException("Property not found", e);
							}
						}

						AbstractSession session = JAXBHelper.getJAXBContext(context).getXMLContext().getSession(klass);
						ClassDescriptor classDescriptor = session.getClassDescriptor(klass);
						DatabaseMapping mapping = classDescriptor.getMappingForAttributeName(f.getName());
						if (mapping == null) {
							throw new RuntimeException("Field '" + f.getName() + "' cannot be annotated with"
									+ " @" + XmlDefaultValue.class.getSimpleName() + ", because it is not mapped"
									+ ", mark it with @" + XmlElement.class.getSimpleName());
						}
						mapping.setAttributeValueInObject(object, value);

						f.setAccessible(accessible);

						/**
						 * For reference, how it worked in JAXB RI:
						 *
						RuntimeNonElement typeInfo = context.getRuntimeTypeInfoSet().getTypeInfo(f.getType());
						Object v;
						try {
							v = typeInfo.getTransducer().parse(value);
						} catch (AccessorException | SAXException e) {
							throw new RuntimeException(e);
						}

						try {
							f.set(object, v);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
						*/
					}
				}

				klass = klass.getSuperclass();
			}
		}
	}
}
