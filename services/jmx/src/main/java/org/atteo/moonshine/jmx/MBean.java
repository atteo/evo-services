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
package org.atteo.moonshine.jmx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Singleton;

import org.atteo.classindex.IndexAnnotated;

/**
 * MBean marker interface.
 *
 * <p>
 * Any annotated classes will be discovered and registered by {@link JMX} service as MBean.
 * By convention any such class should implement an interface with the same name
 * with the suffix <i>MBean</i> added (see <a href="http://download.oracle.com/javase/tutorial/jmx/mbeans/standard.html">JMX tutorial</a>).
 * </p>
 * <p>
 * Additionally annotated class will be registered in Guice with {@link Singleton} scope.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@IndexAnnotated
public @interface MBean {
	/**
	 * Name that the MBean will be registered with.
	 * <p>
	 * If not specified, one will be generated based on full class name of the annotated class.
	 * </p>
	 */
	String name() default "";
}
