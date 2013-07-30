#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013 ${company}.
 */
package ${package};

import java.io.IOException;

import org.atteo.evo.config.IncorrectConfigurationException;
import org.atteo.moonshine.Moonshine;

public class Main {
	public static void main(String[] args) throws IOException, IncorrectConfigurationException {
		Moonshine moonshine = Moonshine.Factory.builder()
				.arguments(args)
				.build();

		moonshine.start();
	}
}
