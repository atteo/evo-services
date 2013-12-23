package org.atteo.moonshine.logback;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * Make Guice inject our logger when a field is annotated with {@link org.atteo.moonshine.logback.Log}
 */
public class SLF4JTypeListener implements TypeListener {
    public <T> void hear(TypeLiteral<T> iTypeLiteral, TypeEncounter<T> iTypeEncounter) {
        for (Field field : iTypeLiteral.getRawType().getDeclaredFields()) {
            if (field.getType() == Logger.class
                    && field.isAnnotationPresent(Log.class)) {
                iTypeEncounter.register(new SLF4JMembersInjector<T>(field));
            }
        }
    }
}
