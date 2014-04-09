
package org.atteo.moonshine.tests;

import javax.inject.Inject;
import javax.inject.Named;

import org.atteo.moonshine.tests.MoonshineConfiguration.Config;
import org.junit.Assert;
import org.junit.Test;

@MoonshineConfiguration(skipDefault = true, forEachConfig = @Config({"/message1.xml", "/message2.xml"}))
public class MoonshineSuiteRunnerTest extends MoonshineTest {

	@Inject
	@Named("message")
	private String message;

	@Test
	public void shouldBeOneOfTwoMessages() {
		if (!message.equals("message1") && !message.equals("message2")) {
			Assert.fail("'message1' or 'message2' expected");
		}
	}

}
