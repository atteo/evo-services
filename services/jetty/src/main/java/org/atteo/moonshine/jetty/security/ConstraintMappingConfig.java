package org.atteo.moonshine.jetty.security;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.config.AbstractConfigurable;
import org.eclipse.jetty.security.ConstraintMapping;

@XmlRootElement(name = "constraint-mapping")
public class ConstraintMappingConfig extends AbstractConfigurable {

	@XmlElement
	private String method;

	@XmlElementWrapper(name = "method-omissions")
	@XmlElement(name = "method")
	private List<String> methodOmissions;

	@XmlElement(name = "path-spec")
	private String pathSpec;

	@XmlElementRef(required = true)
	private ConstraintConfig constraintConfig;

	public ConstraintMapping getConstraintMapping() {
		ConstraintMapping constraintMapping = new ConstraintMapping();

		constraintMapping.setMethod(method);

		if (methodOmissions != null) {
			constraintMapping.setMethodOmissions(methodOmissions.toArray(new String[methodOmissions.size()]));
		}

		constraintMapping.setConstraint(constraintConfig.getConstraint());
		constraintMapping.setPathSpec(pathSpec);

		return constraintMapping;

	}
}
