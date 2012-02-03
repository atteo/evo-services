/*
 * Copyright 2012 Atteo.
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
package org.atteo.evo.config.doclet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.atteo.evo.config.Configurable;
import org.atteo.evo.config.XmlDefaultValue;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Type;
import com.sun.tools.doclets.standard.Standard;

public class ConfigDoclet extends Doclet {
	public static boolean start(RootDoc root) {
		Standard.start(root);
		generateServicesDocumentation(root);
		return true;
	}

	public static int optionLength(String option) {
		return Standard.optionLength(option);
	}

	public static boolean validOptions(String[][] options, DocErrorReporter reporter) {
		return Standard.validOptions(options, reporter);
	}

	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	private static void generateServicesDocumentation(RootDoc root) {
		LinkGenerator linkGenerator = new LinkGenerator();
		linkGenerator.map(root);
		
		for (ClassDoc klass : root.classes()) {
			// TODO: instead check whether it is reachable from Configurable
			if (isSubclass(klass, Configurable.class)) {
				ClassDescription description = analyseClass(klass);
				String result = documentClass(description, linkGenerator);

				File file = new File(klass.qualifiedName().replaceAll("\\.", File.separator) + ".html");
				if (file.exists()) {
					try {
						String content = Files.toString(file, Charset.defaultCharset());
						
						int index = content.indexOf("<!-- =========== FIELD SUMMARY =========== -->");
						if (index == -1) {
							index = content.indexOf("<!-- ======== CONSTRUCTOR SUMMARY ======== -->");
							if (index == -1) {
								System.out.println("Config: Warning: cannot insert service configuration doc");
								continue;
							}
						}
						
						StringBuilder output = new StringBuilder();
						output.append(content.substring(0, index));
						output.append(result);
						output.append(content.substring(index));
						Files.write(output, file, Charset.defaultCharset());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					System.out.println("ConfigDoclet Warning: File not found: " + file.getAbsolutePath());
				}
			}
		}
		updateStyleSheet();
	}

	private static boolean isSubclass(ClassDoc subclass, Class<?> superclass) {
		ClassDoc klass = subclass;

		while (klass != null) {
			if (superclass.getCanonicalName().equals(klass.qualifiedName())) {
				return true;
			}
			klass = klass.superclass();
		}
		return false;
	}

	private static boolean isImplementingInterface(ClassDoc subclass, Class<?> superinterface) {
		ClassDoc klass = subclass;
		if (superinterface.getCanonicalName().equals(klass.qualifiedName())) {
			return true;
		}

		while (klass != null) {
			for (ClassDoc interfaceDoc : klass.interfaces()) {
				if (superinterface.getCanonicalName().equals(interfaceDoc.qualifiedName())) {
					return true;
				}
			}
			klass = klass.superclass();
		}
		return false;
	}

	private static ClassDescription analyseClass(ClassDoc klass) {
		ClassDescription description = new ClassDescription();
		description.setClassDoc(klass);
		analyseClassAnnotations(klass, description);
		analyseFields(klass, description);

		String classComment = klass.commentText();
		int index = classComment.indexOf('.');
		if (index == -1) {
			index = classComment.length();
		}
		String summary = classComment.substring(0, index).trim();
		if (!summary.isEmpty()) {
			description.setSummary(summary);
		}

		return description;
	}

	private static void analyseClassAnnotations(ClassDoc klass, ClassDescription description) {
		if (klass.isAbstract()) {
			description.setTagName("? extends " + klass.simpleTypeName());
		} else {
			description.setTagName("...");
		}
		description.setAccessType(XmlAccessType.PUBLIC_MEMBER);
		for (AnnotationDesc annotation : klass.annotations()) {
			String name = annotation.annotationType().qualifiedName();
			if (XmlRootElement.class.getCanonicalName().equals(name)) {
				String tagName = getAnnotationElementValue(annotation, "name");
				if (tagName != null && ! "##default".equals(tagName)) {
					description.setTagName(tagName);
				} else {
					description.setTagName(klass.simpleTypeName().toLowerCase());
				}
			} else if (XmlAccessorType.class.getCanonicalName().equals(name)) {
				FieldDoc field = getAnnotationElementValue(annotation, "value");
				XmlAccessType accessType = XmlAccessType.valueOf(field.name());
				
				if (accessType != null) {
					description.setAccessType(accessType);
				}
			}
		}
	}

	private static void analyseFields(ClassDoc klass, ClassDescription description) {
		outer: for (FieldDoc field : klass.fields(false)) {
			ElementDescription element = new ElementDescription();
			Type type = field.type();
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterized = (ParameterizedType) type;
				if (isImplementingInterface(parameterized.asClassDoc(), List.class)) {
					type = parameterized.typeArguments()[0];
					element.setCollection(true);
				}
			}
			
			for (AnnotationDesc annotation : field.annotations()) {
				String name = annotation.annotationType().qualifiedName();
				if (XmlTransient.class.getCanonicalName().equals(name)) {
					continue outer;
				} else if (XmlElement.class.getCanonicalName().equals(name)) {
					element.setType(ElementType.ELEMENT);
					analyseElementAnnotation(annotation, element);
					if (element.getDefaultValue() == null) {
						String defaultValue = getAnnotationElementValue(annotation, "defaultValue");
						if (defaultValue != null && !"\u0000".equals(defaultValue)) {
							element.setDefaultValue(defaultValue);
						}
					}
				} else if (XmlAttribute.class.getCanonicalName().equals(name)) {
					element.setType(ElementType.ATTRIBUTE);
					analyseElementAnnotation(annotation, element);
				} else if (XmlElementRef.class.getCanonicalName().equals(name)) {
					element.setType(ElementType.ELEMENT);
					element.setName("? extends " + type.simpleTypeName());
					// 
				} else if (XmlDefaultValue.class.getCanonicalName().equals(name)) {
					String defaultValue = getAnnotationElementValue(annotation, "value");
					element.setDefaultValue(defaultValue);
				} else if (XmlElementWrapper.class.getCanonicalName().equals(name)) {
					String tagName = getAnnotationElementValue(annotation, "name");
					if ("##default".equals(tagName)) {
						tagName = field.name();
					}
					element.setWrapperName(tagName);
				}
			}

			if (element.getType() == null) {
				switch (description.getAccessType()) {
					case PUBLIC_MEMBER:
						if (field.isPublic() && !field.isStatic()) {
							element.setType(ElementType.ELEMENT);
						}
						break;
					case FIELD:
						if (!field.isStatic()) {
							element.setType(ElementType.ELEMENT);
						}
						break;
				}
			}

			if (element.getType() == null) {
				continue;
			}

			if (element.getName() == null) {
				element.setName(field.name());
			}

			if (element.getElementType() == null) {
				element.setElementType(type);
			}

			String fieldComment = field.commentText().trim();
			fieldComment = fieldComment.replace("<p>", "").replace("</p>","");

			element.setComment(fieldComment);
			if (element.getDefaultValue() == null) {
				element.setDefaultValue(getDefaultValue(field.position()));
			}
			description.addElement(element);
		}
	}
	
	private static void analyseElementAnnotation(AnnotationDesc annotation, ElementDescription element) {
		String tagName = getAnnotationElementValue(annotation, "name");
		if (tagName != null && !"##default".equals(tagName)) {
			element.setName(tagName);
		}
		Boolean required = getAnnotationElementValue(annotation, "required");
		if (required != null) {
			element.setRequired(required);
		}
	}


	private static <T> T getAnnotationElementValue(AnnotationDesc annotation, String elementName) {
		for (ElementValuePair pair : annotation.elementValues()) {
			AnnotationTypeElementDoc annotationElement;
			try {
				annotationElement = pair.element();
			} catch (ClassCastException e) {
				// TODO: is this Java bug?
				System.out.println("ConfigDoclet warning: cannot read annotation fields "
						+ annotation.annotationType().name() + ", value = " + pair.value());
				continue;
			}
			if (elementName.equals(annotationElement.name())) {
				return (T) pair.value().value();
			}
		}
		// value not found, search for default value
		AnnotationTypeElementDoc[] elements;
		try {
			elements = annotation.annotationType().elements();
		} catch (ClassCastException e) {
			// TODO: is this Java bug?
			System.out.println("ConfigDoclet warning: cannot read default value for field '" + elementName
					+ "' for annotation type " + annotation.annotationType().toString());
			return null;
		}
		for (AnnotationTypeElementDoc annotationElement : elements) {
			if (annotationElement.name().equals(elementName)) {
				return (T) annotationElement.defaultValue().value();
			}
		}

		throw new RuntimeException("Annotation value not found");
	}

	private static String documentClass(ClassDescription description, LinkGenerator linkGenerator) {
		HtmlWriter writer = new HtmlWriter();

		writer.append("<!-- ======== CONFIGURATION ======== -->\n");
		writer.append("<ul class=\"blockList\">\n");
		writer.append("<li class=\"blockList\"><a name=\"configuration\"><!--   --></a>");
		writer.append("<h3>Configuration</h3>\n");
		writer.append("<ul class=\"blockList\">\n");
		writer.append("<li class=\"blockList syntaxhighlighter\">\n");
		writer.append("<h3>XML</h3>\n");

		if (description.getSummary() != null
				|| ! description.getAttributes().isEmpty()) {
			writer.lt().append("!-- ").newline();
			writer.indent(1);
			writer.append(description.getSummary());
			writer.append(".").newline();
			for (ElementDescription attribute : description.getAttributes()) {
				if (attribute.getComment() == null) {
					continue;
				}
				writer.indent(1);
				writer.append(attribute.getName()).append(" - ");
				writer.append(attribute.getComment()).newline();
			}
			writer.append("--").gt().newline();
		}
		
		String tagName = description.getTagName();

		if (description.getAttributes().isEmpty()) {
			writer.lt().keyword(tagName).gt().newline();
		} else {
			writer.lt().keyword(tagName);
			for (ElementDescription attribute: description.getAttributes()) {
				writer.newline();
				writer.indent(1).keyword(attribute.getName()).append(" = \"");
				writer.defaultValue(attribute.getDefaultValue()).append("\"");
			}
			writer.append("&gt;").newline();
		}
		
		for (ElementDescription element : description.getElements()) {
			writer.comment(element.getComment(), 1);
			writer.indent(1);

			if (element.getWrapperName() != null) {
				writer.lt().keyword(element.getWrapperName()).gt().newline();
				writer.indent(2);
			}
			String url = null;
			if (element.getElementType() != null && element.getElementType() instanceof ClassDoc) {
				url = linkGenerator.getUrl((ClassDoc) element.getElementType(),
						description.getClassDoc().containingPackage());
				if (url != null) {
					writer.append("<a href=\"" + url + "\">");
				}
			}
			
			writer.lt().keyword(element.getName()).gt();
			writer.defaultValue(element.getDefaultValue());
			writer.lt().append("/").keyword(element.getName()).gt();	

			if (url != null) {
				writer.append("</a>");
			}
			if (element.isCollection()) {
				writer.append("     ");
				writer.lt().append("!-- many --").gt();
			}
			writer.newline();

			if (element.getWrapperName() != null) {
				writer.indent(1);
				writer.lt().append("/").keyword(element.getWrapperName()).gt().newline();
			}
		}
		
		writer.lt().append("/").keyword(tagName).gt().newline();

		writer.append("</li>\n</ul>\n");
		writer.append("</li>\n</ul>\n");
		return writer.toString();
	}

	private static void updateStyleSheet() {
		try {
			FileWriter writer = new FileWriter("stylesheet.css", true);
			writer.write("\n");
			writer.write(".keyword {font-weight: bold !important;color: #006699 !important;}\n");
			writer.write(".syntaxhighlighter {font-family: \"Consolas\", \"Bitstream Vera Sans Mono\", \"Courier New\", Courier, monospace !important;}\n");
			writer.write(".syntaxhighlighter a:hover {text-decoration:underline;}\n");
			writer.write(".syntaxhighlighter p {margin: 0px;}\n");
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getDefaultValue(final SourcePosition position) {
		File file = position.file();
		if (file == null || position.line() == 0) {
			return null;
		}

		try {
			return Files.readLines(file, Charset.defaultCharset(), new LineProcessor<String>() {
				private int lineCount = 0;
				private boolean afterEqualSign = false;
				private boolean lastWasSpace = true;
				private StringBuilder builder = new StringBuilder();

				@Override
				public boolean processLine(String line) throws IOException {
					lineCount++;
					
					if (lineCount < position.line()) {
						return true;
					}
					
					int index = 0;
					int virtualIndex = 0;
					
					if (lineCount == position.line() && position.column() > 0) {
						while (virtualIndex < position.column() && index < line.length()) {
							if (line.charAt(index) == '\t') {
								// Javadoc counts tab as 8 characters in size when reporting column position
								virtualIndex += 8;
							} else {
								virtualIndex++;
							}
							index++;
						}
					}

					while (index < line.length()) {
						char ch = line.charAt(index);
						if (ch == ';') {
							return false;
						}	

						if (afterEqualSign) {
							if (!lastWasSpace) {
								builder.append(ch);
							} else {
								if (!Character.isSpaceChar(ch)) {
									builder.append(ch);
								}
							}
							lastWasSpace = Character.isSpaceChar(ch);
						} else if (ch == '=') {
							afterEqualSign = true;
						}
						index++;
					}
					return true;
				}
				
				@Override
				public String getResult() {
					if (builder.length() == 0) {
						return null;
					}
					if (builder.charAt(0) == '"' && builder.charAt(builder.length() -1) == '"') {
						return builder.substring(1, builder.length() - 1);
					}
					return builder.toString();
				}
			});
		} catch (IOException e) {
			System.out.println("Warning: cannot read file: " + file.getAbsolutePath());
			return null;		
		}
	}
}
