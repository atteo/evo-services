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
package org.atteo.evo.janino;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.services.TopLevelService;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import jline.console.ConsoleReader;

@XmlRootElement(name = "java-console")
public class JavaConsole extends TopLevelService {
	private static class ConsoleThread extends Thread {
		@Override
		public void run() {
			try {
				ConsoleReader console = new ConsoleReader();

				while(true) {
					String line  = console.readLine("> ");
					if (line == null) {
						return;
					}

					if (line.isEmpty()) {
						continue;
					}
					
					ExpressionEvaluator evaluator = new ExpressionEvaluator();
					evaluator.setExpressionType(Object.class);
					try {
						evaluator.cook(line);
						Object result = evaluator.evaluate(new Object[] { });
						if (result != null) {
							console.println(result.toString());
						} else {
							console.println("null");
						}
					} catch (CompileException e) {
						console.println(e.getMessage());
					} catch (InvocationTargetException e) {
						console.println("Got exception:\n" + e.getCause().toString());
					} catch (Exception e) {
						e.printStackTrace(new PrintWriter(console.getOutput()));
					}
					console.flush();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Thread thread;
	
	@Override
	public void start() {
		thread = new ConsoleThread();
		thread.start();
	}

	@Override
	public void stop() {
		thread.interrupt();
		try {
			thread.join(1000);
		} catch (InterruptedException e) {
		}
	}
}
