package org.atteo.moonshine.tests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.atteo.moonshine.Moonshine;

/**
 * Can be specified on a test class extending {@link MoonshineTest}.
 * Allows to specify configuration resources to use when starting {@link Moonshine} framework.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MoonshineConfiguration {
	/**
	 * List of resources to use as configuration for {@link Moonshine}.
	 */
	String[] value() default {};

	/**
	 * In-place configuration.
	 */
	String fromString() default "";

	/**
	 * Skip reading default configuration from '/default-config.xml' classpath resource.
	 */
	boolean skipDefault() default false;

    /**
     * if true app modules will be auto registered
     */
    boolean registerAppModules() default false;

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
