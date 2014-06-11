
package org.atteo.moonshine.jetty.security;

import org.atteo.config.Configurable;
import org.eclipse.jetty.security.Authenticator;

public abstract class AuthenticatorConfig extends Configurable {
	public abstract Authenticator getAuthenticator();
}
