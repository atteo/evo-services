
package org.atteo.evo.jetty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.Configurable;

@XmlRootElement(name="mimeType")
public class ResourceHandlerMimeTypeConfig extends Configurable {
	@XmlElement
	private String extension;

	@XmlElement
	private String name;

	public String getExtension() {
		return extension;
	}

	public String getName() {
		return name;
	}

}
