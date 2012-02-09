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
package org.atteo.evo.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.filtering.CompoundPropertyResolver;
import org.atteo.evo.filtering.Filtering;
import org.atteo.evo.filtering.PropertiesPropertyResolver;
import org.atteo.evo.filtering.PropertyNotFoundException;
import org.atteo.evo.filtering.PropertyResolver;
import org.atteo.evo.jaxb.JaxbBindings;
import org.atteo.evo.jaxb.ScopedIdResolver;
import org.atteo.evo.xmlcombiner.XmlCombine;
import org.atteo.evo.xmlcombiner.XmlCombiner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;
import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationsException;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

/**
 * Provides a generic configuration facility which enables the application
 * to read configuration data stored in a number of XML files.
 *
 * <p>
 * Usage:
 * <pre>
 *    Root root = new Configuration().read(Root.class, "first.xml, "second.xml");
 * </pre>
 * </p>
 * <p>
 * The following actions are performed:
 * <ul>
 * <li>{@link JAXBContext} is created for all the classes extending {@link Configurable},</li>
 * <li>provided XML files are parsed into DOM trees,</li>
 * <li>the DOM trees are combined using {@link XmlCombiner} facility,</li>
 * <li>any property references in the form of <code>${name}</code> are substituted
 * with the value generated using {@link PropertyResolver} set by calling
 * {@link #setPropertyResolver(PropertyResolver)}, see {@link Filtering} for details,</li>
 * <li>the result is unmarshalled using {@link Unmarshaller JAXB} into provided root class,</li>
 * <li>the unmarshalled classes are validated using JSR 303 - {@link Validation Bean Validation framework}.</li>
 * </ul>
 * </p>
 * <p>
 * Each of the configuration classes must extend {@link Configurable} which defines some useful
 * XML attributes like {@link Configurable#id id} to uniquely assign the name to configuration element
 * and {@link Configurable#combine combine} attribute to control XML merging behavior.
 * </p>
 * <p>
 * Very useful JAXB annotation to use when defining your configuration file is {@link XmlElementRef}.
 * Although JAXB does not support interfaces you can use abstract superclass to get polymorphism like this:
 * <pre>
 * {@code
 * class Root {
 * .   @XmlElementRef
 * .   @Valid
 *     private List<Color> colors;
 * }
 * 
 * . @XmlRootElement(name = "red")
 * class Red extends Color {
 * }
 * 
 * }
 * </pre>
 * </p>
 */
public class Configuration {
	private JAXBContext context;
	private Binder<Node> binder;
	private final Iterable<Class<? extends Configurable>> klasses;
	private DocumentBuilder builder;
	private Document document;
	private PropertyResolver properties;

	/**
	 * Create Configuration by discovering all {@link Configurable}s.
	 *
	 * <p>
	 * Uses {@link ClassIndex#getSubclasses(Class)} to get {@link Configurable}s.
	 * </p>
	 *
	 * @throws JAXBException when JAXB context creation fails
	 * @throws IOException when index file cannot be read
	 */
	public Configuration() throws IOException {
		this(ClassIndex.getSubclasses(Configurable.class));
	}

	/**
	 * Create Configuration by manually specifying all {@link Configurable}s.
	 * @param klasses list of {@link Configurable} classes.
	 * @throws JAXBException when JAXB context creation fails
	 */
	public Configuration(Iterable<Class<? extends Configurable>> klasses) {
		this.klasses = klasses;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
			context = JAXBContext.newInstance(Iterables.toArray(klasses, Class.class));
			binder = context.createBinder();
			binder.setProperty(IDResolver.class.getName(), new ScopedIdResolver());
			binder.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(ValidationEvent event) {
					return true;
				}
			});
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Cannot configure XML parser", e);
		} catch (IllegalAnnotationsException e) {
			throw new RuntimeException("Cannot configure unmarshaller: " + e.toString());
		} catch (JAXBException e) {
			throw new RuntimeException("Cannot configure unmarshaller", e);
		}
	}

	/**
	 * Generate configuration an XSD schema for the configuration file.
	 * @param filename file to store the schema to
	 * @throws IOException when IO error occurs
	 */
	public void generateSchema(final File filename) throws IOException {
		context.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName)
					throws IOException {
				return new StreamResult(filename);
			}
		});
	}

	/**
	 * Set properties used to filter {@code ${name}} placeholders.
	 * <p>
	 * This method wraps given properties into {@link PropertiesPropertyResolver}
	 * and calls {@link #setPropertyResolver(PropertyResolver)}.
	 * </p>
	 * @param properties properties to filter into configuration files
	 */
	public void setProperties(Properties properties) {
		this.properties = new PropertiesPropertyResolver(properties);
	}

	/**
	 * Set {@link PropertyResolver} used to filter {@code ${name}} placeholders.
	 * <p>
	 * This will override any previously set property resolver.
	 * </p>
	 * 
	 * @param resolver property resolver used in filtering into configuration files
	 * @see CompoundPropertyResolver
	 */
	public void setPropertyResolver(PropertyResolver resolver) {
		this.properties = resolver;
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

			if (parentDocument != null) {
				// Unmarshall the parent document to assign combine attributes annotated on classes
				Element root = parentDocument.getDocumentElement();
				binder.unmarshal(root);
				JaxbBindings.iterate(root, binder, new CombineAssigner());

				// Combine with parent
				document = XmlCombiner.combine(builder, parentDocument, document);
			}
		} catch (UnmarshalException e) {
			if (e.getLinkedException() != null) {
				throw new IncorrectConfigurationException("Cannot parse configuration file: "
						+ e.getLinkedException().getMessage(), e.getLinkedException());
			} else {
				throw new RuntimeException("Cannot parse configuration file", e);
			}
		} catch (JAXBException e) {
			throw new IncorrectConfigurationException("Cannot unmarshall configuration file", e);
		} catch (SAXException e) {
			throw new IncorrectConfigurationException("Cannot parse configuration file", e);
		}
	}

	/**
	 * Unmarshals stored configuration DOM tree as object of the given class.
	 * @param rootClass the class to which unmarshal the DOM tree
	 * @return unmarshalled class tree, or null if no streams were provided
	 * @throws IncorrectConfigurationException if configuration is incorrect
	 */
	public <T extends Configurable> T read(Class<T> rootClass) throws IncorrectConfigurationException {
		if (document == null) {
			return null;
		}
		T result;
		try {
			Filtering.filter(document.getDocumentElement(), properties);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put(JAXBRIContext.ANNOTATION_READER, new FilteringAnnotationReader(properties));
			context = JAXBContext.newInstance(Iterables.toArray(klasses, Class.class), map);
			binder = context.createBinder();
			binder.setProperty(IDResolver.class.getName(), new ScopedIdResolver());
			binder.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(ValidationEvent event) {
					System.out.println("Error in line " + event.getLocator().getLineNumber()
							+ ": " + event.getMessage());
					return false;
				}
			});
			result = rootClass.cast(binder.unmarshal(document.getDocumentElement()));
			JaxbBindings.iterate(document.getDocumentElement(), binder,
					new DefaultsSetter(context, properties));

			ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
			Validator validator = validatorFactory.getValidator();
			Set<ConstraintViolation<T>> violations = validator.validate(result);
			if (!violations.isEmpty()) {
				for (ConstraintViolation<T> violation : violations) {
					System.err.println("Error in configuration file\n"
							+ "          at:   " + violation.getPropertyPath() + "\n"
							+ "   for value:   " + violation.getInvalidValue() + "\n"
							+ "  with error:    " + violation.getMessage());

				}
				throw new IncorrectConfigurationException("Error found while validating"
						+ " configuration file. Errors where listed above.");
			}
		} catch (PropertyNotFoundException e) {
			throw new IncorrectConfigurationException("Cannot filter properties", e);
		} catch (UnmarshalException e) {
			if (e.getLinkedException() != null) {
				throw new IncorrectConfigurationException("Cannot parse configuration file: "
						+ e.getLinkedException().getMessage(), e.getLinkedException());
			} else {
				throw new RuntimeException("Cannot parse configuration file", e);
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
		 * @param object object which DOM element was unmarshalled to.
		 * @param field field which this element was unmarshalled to, can be null
		 */
		@Override
		public void run(Element element, Object object, Field field) {
			if (element.hasAttribute("combine")) {
				return;
			}
			if (field != null) {
				XmlCombine annotation = field.getAnnotation(XmlCombine.class);
				if (annotation != null) {
					Combine value = annotation.value();
					if (value != null) {
						element.setAttribute("combine", value.name());
						return;
					}
				}
			}

			if (object == null) {
				return;
			}

			XmlCombine annotation = object.getClass().getAnnotation(XmlCombine.class);
			if (annotation != null) {
				Combine value = annotation.value();
				if (value != null) {
					element.setAttribute("combine", value.name());
				}
			}
		}
	}

	private static class DefaultsSetter implements JaxbBindings.Runnable {
		private JAXBRIContext context;
		private PropertyResolver properties;

		public DefaultsSetter(JAXBContext context, PropertyResolver properties) {
			this.context = (JAXBContextImpl) context;
			this.properties = properties;
		}

		@Override
		public void run(Element element, Object object, Field field) {
			Class<?> klass = object.getClass();

			while (klass != Object.class) {
				for (Field f : klass.getDeclaredFields()) {
					XmlDefaultValue defaultValue = f.getAnnotation(XmlDefaultValue.class);
					if (defaultValue != null) {
						String value = defaultValue.value();
						try {
							value = Filtering.filter(value, properties);
						} catch (PropertyNotFoundException e) {
							if (field != null) {
								throw new RuntimeException("Property not found for field '"
										+ field.getName() + "'", e);
							} else {
								throw new RuntimeException("Property not found", e);
							}
						}
						RuntimeNonElement typeInfo = context.getRuntimeTypeInfoSet().getTypeInfo(
								f.getType());
						Object v;
						try {
							v = typeInfo.getTransducer().parse(value);
						} catch (AccessorException e) {
							throw new RuntimeException(e);
						} catch (SAXException e) {
							throw new RuntimeException(e);
						}

						boolean accessible = f.isAccessible();
						f.setAccessible(true);
						try {
							f.set(object, v);
						} catch (IllegalArgumentException e) {
							throw new RuntimeException(e);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}

						f.setAccessible(accessible);
					}
				}

				klass = klass.getSuperclass();
			}
		}
	}
}
