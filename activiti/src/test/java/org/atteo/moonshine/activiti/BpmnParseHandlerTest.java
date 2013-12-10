package org.atteo.moonshine.activiti;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.engine.task.Task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class BpmnParseHandlerTest implements BpmnParseHandler {
    protected static final Set<Class<? extends BaseElement>> supportedTypes = new HashSet<Class<? extends BaseElement>>();
    public static int counter;

    static {
        supportedTypes.add(UserTask.class);
    }

    @Override
    public Collection<Class<? extends BaseElement>> getHandledTypes() {
        return supportedTypes;
    }

    @Override
    public void parse(BpmnParse bpmnParse, BaseElement baseElement) {
        counter++;
    }
}
