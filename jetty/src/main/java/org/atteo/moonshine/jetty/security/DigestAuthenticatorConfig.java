
package org.atteo.moonshine.jetty.security;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;

@XmlRootElement(name = "digest-authenticator")
public class DigestAuthenticatorConfig extends AuthenticatorConfig {

	@Override
	public Authenticator getAuthenticator() {
		return new DigestAuthenticator();
	}

}
