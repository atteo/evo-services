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
 * Thrown when error is found in configuration file.
 */
public class ConfigurationException extends MoonshineException {
	private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>ConfigurationException</code> without detail message.
     */
    public ConfigurationException() {
    }

    /**
     * Constructs an instance of <code>ConfigurationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConfigurationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ConfigurationException</code> with the specified detail message
     * and cause.
     * @param msg the detail message.
     * @param cause the cause
     */
    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
