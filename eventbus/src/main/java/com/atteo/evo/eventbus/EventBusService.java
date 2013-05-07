package com.atteo.evo.eventbus;

import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.services.TopLevelService;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * Simple server-side event bus.
 */
@XmlRootElement(name = "eventbus")
public class EventBusService extends TopLevelService {
	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			public void configure() {
				bind(EventBus.class).in(Singleton.class);
			}
		};
	}
}
