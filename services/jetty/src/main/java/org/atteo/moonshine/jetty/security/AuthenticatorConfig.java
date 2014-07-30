
package org.atteo.moonshine.jetty.security;

import org.atteo.config.AbstractConfigurable;
import org.eclipse.jetty.security.Authenticator;

public abstract class AuthenticatorConfig extends AbstractConfigurable {
	public abstract Authenticator getAuthenticator();
}
