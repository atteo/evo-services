/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.firebuglogger;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

public class FireBugAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
	private static ThreadLocal<HttpServletResponse> response =
			new ThreadLocal<HttpServletResponse>();
	private static ThreadLocal<Integer> headerNumber = new ThreadLocal<Integer>();

	private PatternLayout layout;

	public FireBugAppender() {
		layout = new PatternLayout();
		layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
	}

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		layout.setContext(context);
	}

	@Override
	public void start() {
		super.start();
		layout.start();
	}

	public static void setServletResponse(HttpServletResponse resp) {
		response.set(resp);
		headerNumber.set(0);
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (response.get() == null) {
			return;
		}
		
		event.prepareForDeferredProcessing();

		String message = layout.doLayout(event);
		String filename = null;
		Integer line = null;
		if (event.hasCallerData()) {
			StackTraceElement callerData = event.getCallerData()[0];
			filename = callerData.getFileName();
			line = callerData.getLineNumber();
		}

		StringWriter writer = new StringWriter();
		try {
			JsonGenerator builder = new JsonFactory().createJsonGenerator(writer);
			builder.writeStartArray();
			builder.writeStartObject();
			builder.writeFieldName("Type");
			builder.writeString(event.getLevel().toString());
			builder.writeFieldName("File");
			if (filename != null) {
				builder.writeString(filename);
			} else {
				builder.writeNull();
			}
			builder.writeFieldName("Line");
			if (line != null) {
				builder.writeNumber(line);
			} else {
				builder.writeNull();
			}
			builder.writeFieldName("Label");
			builder.writeString("[S]");
			builder.writeEndObject();
			builder.writeString(message);
			builder.writeEndArray();
			builder.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		HttpServletResponse resp = response.get();
		resp.setHeader("X-Wf-Protocol-1", "http://meta.wildfirehq.org/Protocol/JsonStream/0.2");
		resp.setHeader("X-Wf-1-Plugin-1",
				"http://meta.firephp.org/Wildfire/Plugin/FirePHP/Library-FirePHPCore/0.3");
		resp.setHeader("X-Wf-1-Structure-1",
				"http://meta.firephp.org/Wildfire/Structure/FirePHP/FirebugConsole/0.1");

		int number = headerNumber.get();
		int offset = 0;
		int size = writer.getBuffer().length();
		while (size > offset) {
			StringBuilder header = new StringBuilder();
			if (offset == 0) {
				header.append(size);
			}
			header.append("|");
			int end = offset + 4000;
			if (end > size) {
				end = size;
			}
			header.append(writer.getBuffer().substring(offset, end));
			header.append("|");
			offset = end;
			if (offset != size) {
				header.append("\\");
			}

			resp.setHeader("X-Wf-1-1-1-" + number, header.toString());
			number++;
		}


		headerNumber.set(number);
	}
}
