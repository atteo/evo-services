
package org.atteo.moonshine.jetty.security;

import org.atteo.config.Configurable;
import org.eclipse.jetty.security.LoginService;

public abstract class LoginServiceConfig extends Configurable {
	public abstract LoginService getLoginService();
}
