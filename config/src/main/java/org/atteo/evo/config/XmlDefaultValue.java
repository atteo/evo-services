/*
 * Copyright 2011 Atteo.
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
package org.atteo.evo.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.xml.bind.annotation.XmlElement;

/**
 * Specifies the default value for the field when using {@link Configuration}.
 *
 * <p>
 * This differs from {@link XmlElement#defaultValue()} in that it always assigns the default value
 * when the class which contains annotated field is unmarshalled.
 * See <a href="http://jaxb.java.net/guide/Element_default_values_and_unmarshalling.html">this link</a>
 * for an explanation how ordinary {@link XmlElement} default value works.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlDefaultValue {
	/**
	 * Default value to assign to the field.
	 */
	String value();
}
