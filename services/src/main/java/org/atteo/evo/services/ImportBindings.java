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
package org.atteo.evo.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;
import com.google.inject.Module;
import com.google.inject.PrivateModule;

/**
 * Makes the bindings from annotated service available to the local module.
 *
 * <p>
 * The annotation can be used in your service class on a field of {@link Service} type.
 * This will make all the bindings {@link PrivateModule#expose exposed} by the annotated service
 * available in this service {@link Module module} as private bindings.
 * </p>
 * <p>
 * You can additionally annotate the field with any {@link BindingAnnotation binding annotation} to register
 * the binding as annotated with this annotation.
 * </p>
 * <p>
 * Example:
 * <pre>
 * class Robot {
 *     // LegService provides binding for Leg class
 *     &#064;XmlIDREF
 *     &#064;ImportBindings
 *     &#064;Left
 *     LegService legService;
 *
 *     &#064;XmlIDREF
 *     &#064;ImportBindings
 *     &#064;Right
 *     LegService legService;
 *
 *     // Leg class can be injected locally
 *     &#064;Inject
 *     &#064;Left
 *     Leg leg;
 *
 *     &#064;Inject
 *     &#064;Right
 *     Leg leg;
 * }
 * </pre>
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ImportBindings {
}
