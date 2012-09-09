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
package org.atteo.evo.services;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;

import org.atteo.evo.classindex.IndexAnnotated;
import org.atteo.evo.filtering.PropertyResolver;

import com.google.inject.BindingAnnotation;

/**
 * Allows injecting property resolver used by {@link Service} framework.
 *
 * <p>
 * When used on field of type {@link PropertyResolver} and marked with {@link Inject}
 * annotation triggers the injection of property resolver
 * used in the {@link Service} framework.
 * </p>
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@IndexAnnotated
@Documented
public @interface ApplicationProperties {

}
