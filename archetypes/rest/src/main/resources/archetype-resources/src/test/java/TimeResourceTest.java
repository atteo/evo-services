#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013 ${company}.
 */
package ${package};

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.Moonshine;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.atteo.moonshine.webserver.WebServerAddress;
import org.junit.Test;

import com.google.common.io.Resources;

@MoonshineConfiguration(autoConfiguration = true)
public class TimeResourceTest extends MoonshineTest {
	@Inject
	private WebServerAddress address;

	@Test
	public void shouldReturnTime() throws IOException, MalformedURLException {
		System.out.println(Resources.toString(new URL("http://localhost:" + address.getPort() + "/time"), StandardCharsets.UTF_8));
	}
}
