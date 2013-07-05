package org.atteo.moonshine.tests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.atteo.moonshine.services.Services;

/**
 * Can be specified on a test class extending {@link ServicesTest}.
 * Allows to specify configuration resources to use when starting {@link Services} framework.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServicesConfiguration {
	/**
	 * List of resources to use as configuration for {@link Services}.
	 */
	String[] value() default {};
}
