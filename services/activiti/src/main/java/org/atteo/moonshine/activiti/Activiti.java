/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.activiti;


import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.delegate.DelegateInvocation;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.activiti.engine.parse.BpmnParseHandler;
import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Starts Activiti database
 */
@XmlRootElement(name = "activiti")
public class Activiti extends TopLevelService {
    private final Logger log = LoggerFactory.getLogger(Activiti.class);

    @XmlDefaultValue("default")
    String name;

    /**
     * True if to update db schema on boot time:
     * <p/>
     * can be false/true/create/create-drop
     *
     * @see org.activiti.engine.ProcessEngineConfiguration
     * @see org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl
     */
    @XmlDefaultValue("true")
    String dbSchemaUpdate;

    /**
     * True if the job executor should be activated.
     */
    @XmlDefaultValue("false")
    Boolean jobExecutorActivate;

    /**
     * The host of the mail server
     */
    String mailServerHost;

    /**
     * The port of the mail server
     */
    @XmlDefaultValue("25")
    Integer mailServerPort;

    /**
     * History config
     */
    @XmlDefaultValue("audit")
    String history;

    @XmlElementRef
    @XmlElementWrapper(name = "bpmn-parse-handlers")
    List<BpmnParseHandlerConf> bpmnParseHandlers;

    private class ProcessEngineProvider implements Provider<ProcessEngine> {
        @Inject
        private DataSource dataSource;

        @Inject
        Injector injector;

        @Override
        public ProcessEngine get() {
            ProcessEngineConfiguration processEngineConfiguration =
                    JtaProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
                            .setDatabaseSchemaUpdate(dbSchemaUpdate).setDataSource(dataSource)
                            .setJobExecutorActivate(jobExecutorActivate).setHistory(history)
                            .setMailServerHost(mailServerHost).setMailServerPort(mailServerPort);

            if (bpmnParseHandlers != null && !bpmnParseHandlers.isEmpty()) {
                if (processEngineConfiguration instanceof ProcessEngineConfigurationImpl) {
                    ProcessEngineConfigurationImpl pec = (ProcessEngineConfigurationImpl) processEngineConfiguration;
                    if (pec.getPostBpmnParseHandlers() == null) {
                        pec.setPostBpmnParseHandlers(createAndGetHandlers());
                        pec.setDelegateInterceptor(new DelegateInterceptor() {
                            @Override
                            public void handleInvocation(DelegateInvocation invocation) throws Exception {
                                Object target = invocation.getTarget();
                                if ((target instanceof JavaDelegate || target instanceof TaskListener ) ) {
                                    injector.injectMembers(target);
                                }

                                invocation.proceed();
                            }
                        });
                    }
                } else {
                    log.info("BPMN parse handlers are ignored since handlers are only supported with " +
                            "configuration type: ProcessEngineConfigurationImpl");
                }
            }

            ProcessEngine pe = processEngineConfiguration.buildProcessEngine();
            ProcessEngines.registerProcessEngine(pe);

            return pe;
        }

        private List<BpmnParseHandler> createAndGetHandlers() {
            List<BpmnParseHandler> handlers = Lists.newArrayList();
            if (bpmnParseHandlers != null) {
                for (BpmnParseHandlerConf hc : bpmnParseHandlers) {
                    try {
                        BpmnParseHandler parseHandler = hc.getInstance();
                        injector.injectMembers(parseHandler);
                        handlers.add(parseHandler);
                    } catch (Exception e) {
                        log.warn("Could not create handler {}: {}", hc.className, e);
                    }
                }
            }

            return handlers;
        }
    }



    @Override
    public Module configure() {
        return new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ProcessEngine.class).toProvider(new ProcessEngineProvider()).in(Singleton.class);
            }
        };
    }

    public void start() {
        ProcessEngines.init();
    }

    public void stop() {
        ProcessEngines.destroy();
    }
}
