#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013 ${company}.
 */
package ${package};

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.Moonshine;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Test;

public class MessageProviderServiceTest extends MoonshineTest {
	@Inject
	private MessageProvider provider;

	@Test
	public void shouldReturnSpecifiedMessage() {
		assertThat(provider.getMessage()).isEqualTo("Welcome!");
	}
}
