
package org.atteo.moonshine.tests;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.inject.Inject;
import javax.inject.Named;

import org.atteo.moonshine.tests.MoonshineConfiguration.Alternatives;
import org.atteo.moonshine.tests.MoonshineConfiguration.Config;
import org.junit.Assert;
import org.junit.Test;

@MoonshineConfiguration(skipDefault = true, forCartesianProductOf = {
	@Alternatives(value = {
		@Config(id = "one", fromString = ""
				+ "<config>"
				+ "    <simple message='1${letter}'/>"
				+ "</config>"),
		@Config(id = "two", fromString = ""
				+ "<config>"
				+ "    <simple message='2${letter}'/>"
				+ "</config>")}),
	@Alternatives(value = {
		@Config(id = "A", fromString = ""
				+ "<config>"
				+ "    <properties>"
				+ "        <letter>A</letter>"
				+ "    </properties>"
				+ "</config>"),
		@Config(id = "B", fromString = ""
				+ "<config>"
				+ "    <properties>"
				+ "        <letter>B</letter>"
				+ "    </properties>"
				+ "</config>")
	})
})
public class ShouldExecuteForCartesianProductTest extends MoonshineTest {
	@Inject
	@Named("message")
	private String message;

	@Test
	public void shouldBeOneOfTwoMessages() throws IOException {
		if (!message.equals("1A") && !message.equals("1B") && !message.equals("2A") && !message.equals("2B")) {
			Assert.fail();
		}
		Files.write(Paths.get("target/ShouldExecuteForCartesianProduct.txt"),
				message.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

		// then
		// assertion in pom.xml
	}

}
