#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Service which produces message.
 */
@XmlRootElement(name = "provider")
public class MessageProviderService extends TopLevelService {
	@XmlElement
	@XmlDefaultValue("Hello World!")
	private String message;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(MessageProvider.class).toInstance(new MessageProvider() {
					public String getMessage() {
						return message;
					}
				});
			}
		};
	}
}
