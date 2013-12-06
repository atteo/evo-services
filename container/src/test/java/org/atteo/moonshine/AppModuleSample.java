package org.atteo.moonshine;

import com.google.inject.AbstractModule;

/**
 * A sample for a class that is registered as an AppModule
 */
@AppModule
public class AppModuleSample extends AbstractModule {
    @Override
    protected void configure() {
        bind(Head.class);
    }
}
