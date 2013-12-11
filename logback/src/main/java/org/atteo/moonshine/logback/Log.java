package org.atteo.moonshine.logback;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker for injecting slf4j Logger.
 */
@Scope
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Log {

}
