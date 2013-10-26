#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.io.IOException;

import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.moonshine.Moonshine;
import org.atteo.moonshine.MoonshineException;

public class Main {
	public static void main(String[] args) throws IOException, MoonshineException {
		Moonshine moonshine = Moonshine.Factory.builder()
				.arguments(args)
				.build();

		if (moonshine != null) {
			moonshine.start();
		}
	}
}
