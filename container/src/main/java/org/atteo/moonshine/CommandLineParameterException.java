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

/**
 * Thrown when invalid command line parameter is encountered.
 */
public class CommandLineParameterException extends MoonshineException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of <code>CommandLineParameterException</code> without detail message.
	 */
	public CommandLineParameterException() {
	}

	/**
	 * Constructs an instance of <code>CommandLineParameterException</code> with the specified detail message.
	 * @param msg the detail message.
	 */
	public CommandLineParameterException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an instance of <code>CommandLineParameterException</code> with the specified detail message
	 * and cause.
	 * @param msg the detail message.
	 * @param cause the cause
	 */
	public CommandLineParameterException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
