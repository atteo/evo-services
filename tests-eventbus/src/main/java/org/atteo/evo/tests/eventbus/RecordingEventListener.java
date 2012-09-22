package org.atteo.evo.tests.eventbus;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.eventbus.Subscribe;

public class RecordingEventListener {
	private HashMap<Class<?>, ArrayList<Object>> events = new HashMap<Class<?>, ArrayList<Object>>();

	@Subscribe
	public void listen(Object event) {
		ArrayList<Object> eventsOfClass = events.get(event.getClass());
		if (eventsOfClass == null)
			eventsOfClass = new ArrayList<Object>();
		eventsOfClass.add(event);
		events.put(event.getClass(), eventsOfClass);
	}

	public ArrayList<Object> getEvents(Class<?> eventClass) {
		ArrayList<Object> result = events.get(eventClass);

		if (result == null)
			result = new ArrayList<Object>();

		return result;
	}

	public void clearEvents() {
		events.clear();
	}

}
