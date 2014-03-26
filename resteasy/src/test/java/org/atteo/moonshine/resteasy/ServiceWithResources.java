
package org.atteo.moonshine.resteasy;

import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import org.atteo.moonshine.services.ImportService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

@XmlRootElement(name = "service-with-resources")
public class ServiceWithResources extends TopLevelService {

	@ImportService
	private Resteasy resteasy;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(HelloWorldResource.class);

				resteasy.addResource(getProvider(HelloWorldResource.class), HelloWorldResource.class);
			}
		};
	}

}
