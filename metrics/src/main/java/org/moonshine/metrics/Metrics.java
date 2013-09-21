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
package org.moonshine.metrics;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.palominolabs.metrics.guice.InstrumentationModule;

/**
 * Metrics.
 */
@XmlRootElement(name = "metrics")
public class Metrics extends TopLevelService {
	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				// filter out JmxReporter from InstrumentationModule
				final List<Element> elements = new ArrayList<>();
				for (Element element : Elements.getElements(new InstrumentationModule())) {
					element.acceptVisitor(new DefaultElementVisitor<Void>() {
						@Override
						public <T> Void visit(Binding<T> binding) {
							if (binding.getKey().getTypeLiteral().getRawType() != JmxReporter.class) {
								elements.add(binding);
							}
							return super.visit(binding);
						}

						@Override
						protected Void visitOther(Element element) {
							elements.add(element);
							return super.visitOther(element);
						}
					});
				}
				install(Elements.getModule(elements));
			}
		};
	}

	@Inject
	private MetricRegistry registry;

	@Inject(optional = true)
	private MBeanServer mbeanServer;

	private JmxReporter reporter = null;

	@Override
	public void start() {
		if (mbeanServer != null) {
			reporter = JmxReporter.forRegistry(registry).registerWith(mbeanServer).build();
			reporter.start();
		}
	}

	@Override
	public void stop() {
		if (mbeanServer != null) {
			reporter.stop();
			reporter = null;
		}
	}
}
