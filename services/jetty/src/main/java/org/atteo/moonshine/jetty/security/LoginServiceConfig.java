
package org.atteo.moonshine.jetty.security;

import org.atteo.config.AbstractConfigurable;
import org.eclipse.jetty.security.LoginService;

public abstract class LoginServiceConfig extends AbstractConfigurable {
	public abstract LoginService getLoginService();
}
