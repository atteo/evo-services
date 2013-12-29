package org.atteo.moonshine.global_modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.atteo.evo.classindex.IndexAnnotated;

/**
 * Marker interface for applications that require to supply extra Guice modules
 * with business logic bindings rules.
 *
 * Any class that is annotated with this annotation will be added as a Guice module into the application Injector.
 *
 * Note: to register app modules {@link org.atteo.moonshine.Moonshine.Factory#builder().registerAppModules()}} must
 * be invoked.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@IndexAnnotated
public @interface GlobalModule {
}
