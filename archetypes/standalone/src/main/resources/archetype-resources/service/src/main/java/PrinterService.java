#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;

/**
 * Service which prints message.
 */
@XmlRootElement(name = "printer")
public class PrinterService extends TopLevelService {
	@Inject
	private MessageProvider messageProvider;

	@Override
	public void start() {
		System.out.println("Message is: " + messageProvider.getMessage());
	}
}
