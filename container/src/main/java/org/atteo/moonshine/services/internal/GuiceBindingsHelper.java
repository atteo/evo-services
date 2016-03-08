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
package org.atteo.moonshine.services.internal;

import java.util.ArrayList;
import java.util.List;

import org.atteo.moonshine.services.ServiceInfo;

import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.PrivateElements;

public class GuiceBindingsHelper {

	private GuiceBindingsHelper() {
	}

	public static void printServiceElements(List<? extends ServiceInfo> infos) {
		for (ServiceInfo info : infos) {

			System.out.println("Service: " + info.getName() + " {");
			printElements(info.getElements(), 1);
			System.out.println("}");
		}
	}

	public static void printBindings(Iterable<Module> modules) {
		for (Module module : modules) {
			System.out.println("-");
			printBindings(module);
		}
	}

	public static void printBindings(Module module) {
		printElements(Elements.getElements(module), 1);
	}

	public static void printElements(List<Element> elements, final int indentation) {
		for (Element element : elements) {
			printElement(element, indentation);
		}
	}

	public static void printPrivateElements(PrivateElements elements, final int indentation) {
		indent(indentation);
		System.out.println("privateElements {");
		List<Element> exposedElements = new ArrayList<>();
		List<Element> privateElements = new ArrayList<>();
		for (Element element : elements.getElements()) {
			if (element instanceof Binding<?> && elements.getExposedKeys().contains(((Binding)element).getKey())) {
				exposedElements.add(element);
			} else {
				privateElements.add(element);
			}
		}
		if (!exposedElements.isEmpty()) {
			indent(indentation + 1);
			System.out.println("// exposed:");
		}
		for (Element element : exposedElements) {
			printElement(element, indentation + 1);
		}
		if (!privateElements.isEmpty()) {
			indent(indentation + 1);
			System.out.println("// private:");
		}
		for (Element element : privateElements) {
			printElement(element, indentation + 1);
		}
		indent(indentation);
		System.out.println("}");
	}

	public static void printElement(Element element, final int indentation) {
		element.acceptVisitor(new DefaultElementVisitor<Void>() {
			@Override
			public Void visit(PrivateElements privateElements) {
				printPrivateElements(privateElements, indentation);
				return null;
			}

			@Override
			protected Void visitOther(Element element) {
				indent(indentation);
				System.out.println(element);
				return null;
			}
		});
	}

	public static void indent(int n) {
		for (int i = 0; i < n; i++) {
			System.out.print("    ");
		}
	}
}
