package org.atteo.moonshine.jetty.security;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.config.Configurable;
import org.eclipse.jetty.util.security.Constraint;

@XmlRootElement(name = "constraint")
public class ConstraintConfig extends Configurable {

	@XmlElement
	private boolean authenticate = false;

	@XmlElementWrapper(name = "roles")
	@XmlElement(name = "role")
	private List<String> roles;

	@XmlElement
	private String name;

	@XmlElement(name = "data-constraint")
	private int dataConstraint;

	public Constraint getConstraint() {
		Constraint constraint = new Constraint();

		constraint.setAuthenticate(authenticate);

		if (roles != null) {
			constraint.setRoles(roles.toArray(new String[roles.size()]));
		}

		constraint.setName(name);
		constraint.setDataConstraint(dataConstraint);

		return constraint;
	}
}
