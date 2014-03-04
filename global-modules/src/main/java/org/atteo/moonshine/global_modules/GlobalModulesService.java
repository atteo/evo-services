/*
 * Copyright 2013 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atteo.moonshine.global_modules;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.inject.Singleton;
import org.atteo.classindex.ClassIndex;
import org.atteo.moonshine.TopLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Registers application modules that are annotated with {@link GlobalModule}
 */
@XmlRootElement(name = "global-modules")
@Singleton
public class GlobalModulesService extends TopLevelService {
    private final static Logger logger = LoggerFactory.getLogger(GlobalModulesService.class);

    @Override
    public Module configure() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                for (Class<?> moduleClass : ClassIndex.getAnnotated(GlobalModule.class)) {
                    if (!Module.class.isAssignableFrom(moduleClass)) {
                        throw new IllegalStateException("Class " + moduleClass.getName()
                                + " is annotated as @" + GlobalModule.class.getSimpleName()
                                + " but doesn't implement "
                                + Module.class.getCanonicalName());
                    }

                    logger.trace("Found @AppModule [{}].", moduleClass.getName());
                    Module module;
                    try {
                        module = (Module)moduleClass.newInstance();
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new IllegalStateException("Could not instantiate AppModule {}" + moduleClass.getName(), e);
                    }
                    install(module);
                }
            }
        };
    }
}
