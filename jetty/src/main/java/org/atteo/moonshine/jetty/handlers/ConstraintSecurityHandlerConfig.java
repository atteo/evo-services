package org.atteo.moonshine.jetty.handlers;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.jetty.security.AuthenticatorConfig;
import org.atteo.moonshine.jetty.security.ConstraintMappingConfig;
import org.atteo.moonshine.jetty.security.LoginServiceConfig;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;

@XmlRootElement(name = "constraint-security")
public class ConstraintSecurityHandlerConfig extends HandlerWrapperConfig {

	@XmlElementRef(required = true)
	private LoginServiceConfig loginServiceConfig;

	@XmlElementRef(required = true)
	private AuthenticatorConfig authenticatorConfig;

	@XmlElement(required = true, name = "realm-name")
	private String realmName;

	@XmlElementWrapper(name = "constraint-mappings")
	@XmlElementRef
	private List<ConstraintMappingConfig> constraintMappings = new ArrayList<>();

	@Override
	protected HandlerWrapper createHandler() {
		ConstraintSecurityHandler handler = new ConstraintSecurityHandler();

		handler.setLoginService(loginServiceConfig.getLoginService());
		handler.setRealmName(realmName);
		handler.setAuthenticator(authenticatorConfig.getAuthenticator());

		for (ConstraintMappingConfig constraintMappingConfig : constraintMappings) {
			handler.addConstraintMapping(constraintMappingConfig.getConstraintMapping());
		}

		return handler;
	}

}
