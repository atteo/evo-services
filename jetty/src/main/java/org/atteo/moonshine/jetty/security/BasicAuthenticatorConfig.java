
package org.atteo.moonshine.jetty.security;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;

@XmlRootElement(name = "basic-authenticator")
public class BasicAuthenticatorConfig extends AuthenticatorConfig {

	@Override
	public Authenticator getAuthenticator() {
		return new BasicAuthenticator();
	}

}
