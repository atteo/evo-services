/*
 * Copyright 2013 Atteo.
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
package org.atteo.moonshine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.classindex.ClassFilter;
import org.atteo.classindex.ClassIndex;
import org.atteo.config.Configuration;
import org.atteo.config.IncorrectConfigurationException;
import org.atteo.config.XmlDefaultValue;
import org.atteo.config.XmlUtils;
import org.atteo.filtering.CompoundPropertyResolver;
import org.atteo.filtering.EnvironmentPropertyResolver;
import org.atteo.filtering.OneOfPropertyResolver;
import org.atteo.filtering.PropertyResolver;
import org.atteo.filtering.SystemPropertyResolver;
import org.atteo.filtering.XmlPropertyResolver;
import org.atteo.moonshine.directories.FileAccessor;
import org.atteo.moonshine.services.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;

public class ConfigurationReader {
	public final static String SCHEMA_FILE_NAME = "schema.xsd";
	public final static String CONFIG_FILE_NAME = "config.xml";
	public final static String DEFAULT_CONFIG_RESOURCE_NAME = "/default-config.xml";

	private final Configuration configuration = new Configuration();
	private final CompoundPropertyResolver customPropertyResolvers = new CompoundPropertyResolver();
	private PropertyResolver propertyResolver = null;
	private final FileAccessor fileAccessor;

	public ConfigurationReader(FileAccessor fileAccessor) {
		this.fileAccessor = fileAccessor;
	}

	public void filter() throws IncorrectConfigurationException {
		Element propertiesElement = null;
		if (configuration.getRootElement() != null) {
			NodeList nodesList = configuration.getRootElement().getElementsByTagName("properties");
			if (nodesList.getLength() == 1) {
				propertiesElement = (Element) nodesList.item(0);
			}
		}

		propertyResolver = new CompoundPropertyResolver(
				new OneOfPropertyResolver(),
				new SystemPropertyResolver(),
				new EnvironmentPropertyResolver(),
				new XmlPropertyResolver(propertiesElement, false),
				customPropertyResolvers,
				new XmlPropertyResolver(configuration.getRootElement(), true));

		configuration.filter(propertyResolver);
	}

	public Config read() throws IncorrectConfigurationException {
		return configuration.read(Config.class);
	}

	public PropertyResolver getPropertyResolver() {
		return propertyResolver;
	}

	public void combineImplicitConfiguration() throws IncorrectConfigurationException {
		Iterable<Class<? extends Service>> services = ClassFilter.only()
				.topLevel()
				.withoutModifiers(Modifier.ABSTRACT)
				.satisfying(new ClassFilter.Predicate() {
					@Override
					public boolean matches(Class<?> type) {
						return TopLevelService.class.isAssignableFrom(type);
					}
				})
				.satisfying(new ClassFilter.Predicate() {
					@Override
					public boolean matches(Class<?> type) {
						return !containsRequiredFieldWithoutDefault(type);
					}
				})
				.satisfying(new ClassFilter.Predicate() {
					@Override
					public boolean matches(Class<?> type) {
						return !type.isAnnotationPresent(ExplicitService.class);
					}
				})
				.from(ClassIndex.getSubclasses(Service.class));

		StringBuilder builder = new StringBuilder();
		builder.append("<config>\n");
		for (Class<? extends Service> service : services) {
			String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, service.getSimpleName());
			XmlRootElement xmlRootElement = service.getAnnotation(XmlRootElement.class);
			if (xmlRootElement != null && !"##default".equals(xmlRootElement.name())) {
				name = xmlRootElement.name();
			}
			builder.append("\t<");
			builder.append(name);
			builder.append("/>\n");
		}
		builder.append("</config>\n");

		combineConfigurationFromString(builder.toString());
	}

	/**
	 * Reads configuration from '/default-config.xml' resource.
	 */
	public void combineDefaultConfiguration() {
		try {
			combineConfigurationFromResource(DEFAULT_CONFIG_RESOURCE_NAME, false);
		} catch (IOException | IncorrectConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads configuration from config.xml files found in ${configDirs} and ${configHome} directories.
	 * @throws IncorrectConfigurationException when configuration is incorrect
	 * @throws IOException when cannot read resource
	 */
	public void combineConfigDirConfiguration() throws IncorrectConfigurationException, IOException {
		for (Path path : fileAccessor.getConfigFiles(CONFIG_FILE_NAME)) {
			try (InputStream stream = Files.newInputStream(path, StandardOpenOption.READ)) {
				combineConfigurationFromStream(stream);
			}
		}
	}

	/**
	 * Reads configuration from given resource.
	 * @param resourcePath path to the resource
	 * @throws IncorrectConfigurationException when configuration is incorrect
	 * @throws IOException when cannot read resource
	 */
	public void combineConfigurationFromResource(String resourcePath, boolean throwIfNotFound)
			throws IncorrectConfigurationException, IOException {
		// TODO: what if more than one resource with given name?
		try(InputStream stream = getClass().getResourceAsStream(resourcePath)) {
			if (stream != null) {
				configuration.combine(stream);
			} else if (throwIfNotFound) {
				throw new RuntimeException("Configuration resource not found: " + resourcePath);
			}
		}
	}

	public void combineConfigurationFromStream(InputStream stream)
			throws IncorrectConfigurationException, IOException {
		configuration.combine(stream);
	}

	/**
	 * Reads configuration from given file.
	 * @param file file with configuration
	 * @param throwIfNotFound whether to throw exception if file is missing
	 * @throws IncorrectConfigurationException when configuration is incorrect
	 * @throws IOException when cannot read file
	 */
	public void combineConfigurationFromFile(File file, boolean throwIfNotFound)
			throws IncorrectConfigurationException, IOException {
		if (!file.exists()) {
			if (throwIfNotFound) {
				throw new RuntimeException("Configuration file not found: " + file.getAbsolutePath());
			} else {
				return;
			}
		}
		try(InputStream stream = new FileInputStream(file)) {
			configuration.combine(stream);
		}
	}

	/**
	 * Reads configuration from given string.
	 * @param string string with configuration
	 * @throws IncorrectConfigurationException when configuration is incorrect
	 */
	public void combineConfigurationFromString(String string) throws IncorrectConfigurationException {
		try (InputStream stream = new ByteArrayInputStream(string.getBytes(Charsets.UTF_8))) {
			configuration.combine(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String printCombinedXml() {
		return XmlUtils.prettyPrint(configuration.getRootElement());
	}

	public void addCustomPropertyResolver(PropertyResolver resolver) {
		customPropertyResolvers.addPropertyResolver(resolver);
	}

	public void generateTemplateConfigurationFile() throws FileNotFoundException, IOException {
		Path schemaPath = fileAccessor.getWritebleConfigFile(SCHEMA_FILE_NAME);
		Files.createDirectories(schemaPath.getParent());
		configuration.generateSchema(schemaPath.toFile());

		Path configPath = fileAccessor.getWritebleConfigFile(CONFIG_FILE_NAME);
		if (Files.exists(configPath)) {
			return;
		}
		try (Writer writer = Files.newBufferedWriter(configPath, Charsets.UTF_8)) {
			writer.append("<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
					+ " xsi:noNamespaceSchemaLocation=\"" + SCHEMA_FILE_NAME
					+ "\">\n</config>\n");
		}
	}

	private static boolean containsRequiredFieldWithoutDefault(Class<?> type) {
		while (type != Object.class) {
			for (Field field : type.getDeclaredFields()) {
				if (field.isAnnotationPresent(XmlDefaultValue.class)) {
					continue;
				}
				XmlElement annotation = field.getAnnotation(XmlElement.class);
				XmlElementRef annotation2 = field.getAnnotation(XmlElementRef.class);
				XmlAttribute annotation3 = field.getAnnotation(XmlAttribute.class);
				if (annotation != null && annotation.required()) {
					return true;
				}
				if (annotation2 != null && annotation2.required()) {
					return true;
				}
				if (annotation3 != null && annotation3.required()) {
					return true;
				}
			}
			type = type.getSuperclass();
		}
		return false;
	}
}
