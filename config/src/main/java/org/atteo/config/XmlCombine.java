/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.atteo.xmlcombiner.CombineChildren;
import org.atteo.xmlcombiner.CombineSelf;
import org.atteo.xmlcombiner.XmlCombiner;

/**
 * Specifies how to combine XML nodes corresponding to the annotated class
 * or to the annotated field.
 *
 * <p>
 * The default behavior is {@link CombineSelf#MERGE} for both self and children.
 * </p>
 * <p>
 * For instance the following code will instruct {@link XmlCombiner} to append the entries in the 'append' list:
 * <pre>
 * {@code
 * public class Root {
 * .   @XmlCombine(children = CombineChildren.APPEND)
 * .   @XmlElementWrapper(name = "append")
 * .   @XmlElementRef
 *     List<Entry> append;
 * }
 * }
 * </pre>
 * </p>
 */
@Documented
@Target({ ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlCombine {
	// Using full name in default value because of a bug in Java 6
	// http://bugs.sun.com/view_bug.do?bug_id=6512707
	CombineSelf self() default org.atteo.xmlcombiner.CombineSelf.MERGE;

	CombineChildren children() default org.atteo.xmlcombiner.CombineChildren.MERGE;
}
