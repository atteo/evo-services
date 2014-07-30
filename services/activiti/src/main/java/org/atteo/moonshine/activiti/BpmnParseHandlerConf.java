package org.atteo.moonshine.activiti;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.parse.BpmnParseHandler;
import org.atteo.config.AbstractConfigurable;


/**
 * Configuration for {@link org.activiti.engine.parse.BpmnParseHandler}
 */
@XmlRootElement(name = "handler")
public class BpmnParseHandlerConf extends AbstractConfigurable {
    @XmlElement
    protected String className;

    public Class<BpmnParseHandler> getBpmnParseHandler() throws ClassNotFoundException {
        return (Class<BpmnParseHandler>) Class.forName(className);
    }

    public BpmnParseHandler getInstance() throws Exception {
        return getBpmnParseHandler().newInstance();
    }
}
