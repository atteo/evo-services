package org.atteo.moonshine.logback;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class LoggerNameFilter extends AbstractMatcherFilter<LoggingEvent> {

	private String loggerName;

	@Override
	public FilterReply decide(LoggingEvent event) {
		if (event.getLoggerName().startsWith(loggerName)) {
			return onMatch;
		} else {
			return onMismatch;
		}
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public void start() {
		if (loggerName != null) {
			super.start();
		}
	}

}