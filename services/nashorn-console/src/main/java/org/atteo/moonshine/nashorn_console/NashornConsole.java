/*
 * Copyright 2012 Atteo.
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
package org.atteo.moonshine.nashorn_console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ServiceInfo;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.PrivateElements;

import jline.TerminalFactory;
import jline.console.ConsoleReader;

/**
 * Nashorn Console.
 */
@XmlRootElement(name = "nashorn-console")
public class NashornConsole extends TopLevelService {
	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
			}
		};
	}

	@Inject
	private Injector injector;

	@Inject
	private List<? extends ServiceInfo> services;

	public class SimpleInjector {
		public Object get(String name) {
			List<Binding<?>> results = new ArrayList<>();
			for (Map.Entry<Key<?>, Binding<?>> entry : injector.getParent().getAllBindings().entrySet()) {
				if (entry.getKey().toString().contains(name)) {
					results.add(entry.getValue());
				}
			}

			if (results.isEmpty()) {
				System.out.println("No matching objects found");
				return null;
			} else if (results.size() > 1) {
				System.out.println("More than one matching objects found");
				return null;
			}

			return results.get(0).getProvider().get();
		}

		public Object get(Class<?> klass) {
			List<Binding<?>> results = new ArrayList<>();
			for (Map.Entry<Key<?>, Binding<?>> entry : injector.getParent().getAllBindings().entrySet()) {
				if (entry.getKey().getTypeLiteral().getType() == klass) {
					results.add(entry.getValue());
				}
			}

			if (results.isEmpty()) {
				System.out.println("No matching objects found");
				return null;
			} else if (results.size() > 1) {
				System.out.println("More than one matching objects found");
				return null;
			}

			return results.get(0).getProvider().get();
		}

		private String getBindingsList() {
			final StringBuilder builder = new StringBuilder();

			for (ServiceInfo service : services) {
				builder.append(service.getName());
				builder.append("\n");

				for (Element element : service.getElements()) {
					element.acceptVisitor(new DefaultElementVisitor<Void>() {
						@Override
						public <T> Void visit(Binding<T> binding) {
							builder.append("    ");
							builder.append(binding.getKey());
							builder.append("\n");
							return null;
						}

						@Override
						public Void visit(PrivateElements elements) {
							for (Element element : elements.getElements()) {
								if (element instanceof Binding<?>
										&& elements.getExposedKeys().contains(((Binding)element).getKey())) {
									Binding<?> binding = ((Binding)element);
									builder.append("    ");
									if (binding.getKey().getAnnotation() != null) {
										builder.append(binding.getKey().getAnnotation());
										builder.append(" ");
									}
									builder.append(binding.getKey().getTypeLiteral());
									builder.append("\n");
								}
							}
							return null;
						}
					});
				}
			}
		
			builder.append("\nUse injector.get(...) to retrieve");

			return builder.toString();
		}

		public void list() {
			System.out.println(getBindingsList());
		}

		@Override
		public String toString() {
			return "SimpleInjector (hint: use injector.list())";
		}
	}

	private class ConsoleThread extends Thread {
		@Override
		public void run() {
			try {
				System.setProperty("nashorn.args", "-scripting");
				ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
				ScriptEngine nashorn = scriptEngineManager.getEngineByName("nashorn");
				if (nashorn == null) {
					throw new RuntimeException("Nashorn Javascript engine not found, this service requires Java 8");
				}
				Bindings bindings = nashorn.createBindings();
				bindings.put("injector", new SimpleInjector());
				nashorn.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
				ConsoleReader console = new ConsoleReader();
				console.setPrompt("js> ");
				String line;
				while (!Thread.currentThread().isInterrupted() && (line = console.readLine()) != null) {
					try {
						Object result = nashorn.eval(line);
						if (result != null) {
							System.out.println(result);
						}
					} catch (ScriptException e) {
						System.out.println("Error: " + e.getMessage());
					} catch (RuntimeException e) {
						System.out.println("Runtime error: " + e.getMessage());
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					TerminalFactory.get().restore();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				System.exit(0);
			}
		}
	}

	private Thread thread = null;

	@Override
	public void start() {
		thread = new ConsoleThread();
		thread.start();
	}

	@Override
	public void stop() {
		thread.interrupt();

		try {
			thread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
