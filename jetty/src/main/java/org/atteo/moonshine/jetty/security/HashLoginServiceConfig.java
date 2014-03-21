package org.atteo.moonshine.jetty.security;

import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;

@XmlRootElement(name = "hash-login-service")
public class HashLoginServiceConfig extends LoginServiceConfig {

	@XmlElement
	private String name;

	@XmlElement
	private String config;

	@Override
	public LoginService getLoginService() {
		URL url = getClass().getResource(config);

		if (url == null) {
			throw new RuntimeException("Resource not found: " + config);
		}

		return new HashLoginService(name, url.toString());
	}

}
