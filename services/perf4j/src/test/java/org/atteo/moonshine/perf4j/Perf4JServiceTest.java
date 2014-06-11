/*
 * Copyright 2013 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.moonshine.perf4j;

import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.perf4j.StopWatch;
import org.perf4j.aop.Profiled;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

@MoonshineConfiguration(autoConfiguration = true)
public class Perf4JServiceTest extends MoonshineTest {
	@Profiled
	protected void profiledMethod() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Test
	public void shouldMessure() {
		// given
		Logger logger = (Logger) LoggerFactory.getLogger(StopWatch.DEFAULT_LOGGER_NAME);

		@SuppressWarnings("unchecked")
		final Appender<ILoggingEvent> mockAppender = Mockito.mock(Appender.class);
		Mockito.when(mockAppender.getName()).thenReturn("MOCK");
		logger.addAppender(mockAppender);

		// when
		profiledMethod();

		// then
		Mockito.verify(mockAppender).doAppend(Mockito.argThat(new ArgumentMatcher<ILoggingEvent>() {
			@Override
			public boolean matches(Object argument) {
				String message = ((ILoggingEvent) argument).getFormattedMessage();
				int startIndex = message.indexOf("time[");
				if (startIndex == -1) {
					return false;
				}
				int endIndex = message.indexOf(']', startIndex);
				if (endIndex == -1) {
					return false;
				}
				int time = Integer.parseInt(message.substring(startIndex + "time[".length(), endIndex));

				return time >= 100;
			}
		}));
	}
}
