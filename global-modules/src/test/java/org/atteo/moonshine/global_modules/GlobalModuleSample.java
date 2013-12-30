package org.atteo.moonshine.global_modules;

import com.google.inject.AbstractModule;

/**
 * A sample for a class that is registered as an AppModule
 */
@GlobalModule
public class GlobalModuleSample extends AbstractModule {
    @Override
    protected void configure() {
        bind(String.class).toInstance("Hello World!");
    }
}
