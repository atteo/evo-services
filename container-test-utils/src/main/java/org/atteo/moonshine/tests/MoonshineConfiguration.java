package org.atteo.moonshine.tests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.atteo.moonshine.Moonshine;

/**
 * Can be specified on a test class extending {@link MoonshineTest}. Allows to
 * specify configuration resources to use when starting {@link Moonshine}
 * framework.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MoonshineConfiguration {

	/**
	 * Represents one possible configuration.
	 */
	public @interface Config {
		/**
		 * Configuration name.
		 */
		String id();

		/**
		 * List of resources to use as configuration for {@link Moonshine}.
		 */
		String[] value() default {};

		/**
		 * In-place configuration.
		 */
		String fromString() default "";
	}

	/**
	 * Represents a list of configurations from which at most only one will be used at given time.
	 */
	public @interface Alternatives {
		Config[] value() default {};
	}

	/**
	 * List of resources to use as configuration for {@link Moonshine}.
	 */
	String[] value() default {};

	/**
	 * Executes the test multiple times. Once for each provided {@link Config}.
	 * <p>
	 * <pre>
	 * For instance:
	 * &#064;MoonshineConfiguration(forEach =
	 *     &#064;Config("/a.xml"),
	 *     &#064;Config("/b.xml")
	 * )
	 * </pre>
	 * This will execute the test 2 times. Once with a.xml and the second tiem with b.xml configuration file.
	 * </p>
	 */
	Config[] forEach() default {};

	/**
	 * Executes the test multiple times. For each execution one {@link Config} will be taken from
	 * each {@link Alternatives} list. The test will be run for every possible combination of Configs.
	 * <p>
	 * For instance:
	 * <pre>
	 * &#064;MoonshineConfiguration(forCartesianProductOf =
	 *      &#064;Alternatives(
	 *          &#064;Config("/a.xml"),
	 *          &#064;Config("/b.xml")
	 *      ),
	 *      &#064;Alternatives(
	 *          &#064;Config("/1.xml"),
	 *          &#064;Config("/2.xml"),
	 *          &#064;Config("/3.xml")
	 *      )
	 * )
	 * </pre>
	 *
	 * This will execute the test 6 times for
	 * <ol>
	 *     <li>a.xml combined with 1.xml</li>
	 *     <li>a.xml combined with 2.xml</li>
	 *     <li>a.xml combined with 3.xml</li>
	 *     <li>b.xml combined with 1.xml</li>
	 *     <li>b.xml combined with 2.xml</li>
	 *     <li>b.xml combined with 3.xml</li>
	 * </p>
	 *
	 */
	Alternatives[] forCartesianProductOf() default {};

	/**
	 * In-place configuration.
	 */
	String fromString() default "";

	/**
	 * Allows to enable auto-configuration.
	 * <p>
	 * Auto configuration is automatically generated and consists of every top-level service found on classpath
	 * which does not require any configuration. It is always stored in ${configHome}/auto-config.xml
	 * file.
	 * </p>
	 */
	boolean autoConfiguration() default false;

	/**
	 * Skip reading default configuration from '/default-config.xml' classpath resource.
	 */
	boolean skipDefault() default false;

	/**
	 * If true, a request (scope) per entire test class will be created,
	 * default means that a request is created per method.
	 */
	boolean oneRequestPerClass() default false;

	/**
	 * Command line arguments.
	 */
	String[] arguments() default {};

	/**
	 * Specifies external configurator for Moonshine.
	 */
	Class<? extends MoonshineConfigurator> configurator() default MoonshineConfigurator.class;
}
