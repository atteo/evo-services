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
package org.atteo.evo.config;

/**
 * Describes how to combine two XML nodes.
 */
public enum Combine {
	/**
	 * Override attributes, merge subelements.
	 *
	 * This is the default.
 	 * Attributes with the same name will be overridden, others will be appended.
	 * Those subelements which can be uniquely paired using name and id
	 * will be combined recursively, those that cannot be paired will be appended.<br/>
	 * Example:<br/>
	 * First:
	 * <pre>
	 * {@code
	 *  <config a="1" b="1">
	 *     <service id="1" name="1" description="1"/>
	 *     <service name="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="1"/>
	 *  </config>
	 * }
	 * </pre>
	 * Second:
	 * <pre>
	 * {@code
	 *  <config b="2" c="2">
	 *     <service id="1" name="2"/>
	 *     <service name="2"/>
	 *     <not_paired name="2"/>
	 *     <not_paired name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 * Result:
	 * <pre>
	 * {@code
	 *  <config a="a" b="2" c="2">
	 *     <service id="1" name="2" description="1"/>
	 *     <service name="2"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="2"/>
	 *     <not_paired name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 */
	MERGE,

	/**
	 * Remove entire element with attributes and children.
	 *
	 * Example:<br/>
	 * First:
	 * <pre>
	 * {@code
	 *  <config a="1" b="1">
	 *     <service combine="REMOVE" id="1" name="1" description="1"/>
	 *     <service name="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="1"/>
	 *  </config>
	 * }
	 * </pre>
	 * Second:
	 * <pre>
	 * {@code
	 *  <config b="2" c="2">
	 *     <service id="1" name="2"/>
	 *     <service combine="REMOVE" name="2"/>
	 *     <not_paired name="2"/>
	 *     <not_paired combine="REMOVE" name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 * Result:
	 * <pre>
	 * {@code
	 *  <config a="a" b="2" c="2">
	 *     <not_paired name="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 */
	REMOVE,

	/**
	 * Merge if child node exists, otherwise remove.
	 * Example:<br/>
	 * First:
	 * <pre>
	 * {@code
	 *  <config a="1" b="1">
	 *     <service combine="DEFAULTS" id="1" name="1" description="1"/>
	 *     <service combine="DEFAULTS" name="1"/>
	 *     <not_paired combine="DEFAULTS" name="1"/>
	 *     <not_paired name="1"/>
	 *  </config>
	 * }
	 * </pre>
	 * Second:
	 * <pre>
	 * {@code
	 * <config b="2" c="2">
	 *     <service id="1" name="2"/>
	 *     <not_paired name="2"/>
	 *     <not_paired combine="DEFAULTS" name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 * Result:
	 * <pre>
	 * {@code
	 * <config a="a" b="2" c="2">
	 *     <service id="1" name="2" description="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 */
	DEFAULTS,

	/**
	 * Override attributes, append subelements.
	 * Example:<br/>
	 * First:
	 * <pre>
	 * {@code
	 * <config a="1" b="1">
	 *     <service id="1" name="1" description="1"/>
	 *     <service name="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="1"/>
	 *  </config>
	 * }
	 * </pre>
	 * Second:
	 * <pre>
	 * {@code
	 * <config b="2" c="2">
	 *     <service id="1" name="2"/>
	 *     <service name="2"/>
	 *     <not_paired name="2"/>
	 *     <not_paired name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 * Result:
	 * <pre>
	 * {@code
	 * <config a="a" b="2" c="2">
	 *     <service id="1" name="1" description="1"/>
	 *     <service name="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="1"/>
	 *     <service id="1" name="2"/>
	 *     <service name="2"/>
	 *     <not_paired name="2"/>
	 *     <not_paired name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 */
	APPEND,

	/**
	 * Override attributes and children elements.
	 * If child elements exist
	 *
	 * Example:<br/>
	 * First:
	 * <pre>
	 * {@code
	 * <config a="1" b="1">
	 *     <service id="1" name="1" description="1"/>
	 *     <service name="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="1"/>
	 *  </config>
	 * }
	 * </pre>
	 * Second:
	 * <pre>
	 * {@code
	 * <config b="2" c="2">
	 *     <service id="1" name="2"/>
	 *     <service name="2"/>
	 *     <not_paired name="2"/>
	 *     <not_paired name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 * Result:
	 * <pre>
	 * {@code
	 * <config a="a" b="2" c="2">
	 *     <service id="1" name="2" description="1"/>
	 *     <service name="2"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="1"/>
	 *     <not_paired name="2"/>
	 *     <not_paired name="2"/>
	 *  </config>
	 * }
	 * </pre>
	 */
	OVERRIDE
}

