package org.atteo.moonshine.logback;

import com.google.inject.MembersInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Custom members injector for instantiating a logger with the declaring class name
 */
public class SLF4JMembersInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final Logger logger;

    public SLF4JMembersInjector(Field field) {
        this.field = field;
        //Call the logger as the name of the injected field's declaring class name
        this.logger = LoggerFactory.getLogger(field.getDeclaringClass());
        //requires so it would be possible to replace the field value later manually in the injectMembers() method.
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(T t) {
        try {
            field.set(t, logger);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
