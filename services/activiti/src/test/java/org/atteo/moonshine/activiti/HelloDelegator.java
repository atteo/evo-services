package org.atteo.moonshine.activiti;

import com.google.inject.Inject;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 */
public class HelloDelegator implements JavaDelegate {
    @Inject
    ProcessEngine processEngine;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (processEngine == null)
            throw new IllegalStateException("Expected processEngine to be injected");
    }
}
