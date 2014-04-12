
package org.atteo.moonshine.tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.tests.MoonshineConfiguration.Config;
import org.junit.Assert;
import org.junit.Test;

@MoonshineConfiguration(skipDefault = true, forEach = {
		@Config(id = "A", fromString = ""
				+ "<config>"
				+ "    <simple message='A'/>"
				+ "</config>"),
		@Config(id = "B", fromString = ""
				+ "<config>"
				+ "    <simple message='B'/>"
				+ "</config>")
})
public class ShouldExecuteForEachConfigTest extends MoonshineTest {
	@Inject
	@Named("message")
	private String message;

	@Inject
	@EnabledConfigs
	private List<String> enabledConfigs;

	@Test
	public void shouldBeOneOfTwoMessages() throws IOException {
		// when
		if (!message.equals("A") && !message.equals("B")) {
			Assert.fail("'A' or 'B' expected");
		}
		Files.write(Paths.get("target/ShouldExecuteForEachConfig.txt"),
				message.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

		// then
		assertThat(enabledConfigs).isNotNull().isNotEmpty();
		// additional assertion in pom.xml
	}

}
