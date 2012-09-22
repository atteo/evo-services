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
package org.atteo.evo.jaxb;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import org.atteo.evo.config.Configurable;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.xml.bind.IDResolver;

/**
 * Resolve Id references in the scope of the expected class.
 *
 * <p>
 * @see <a href="http://weblogs.java.net/blog/2005/08/15/pluggable-ididref-handling-jaxb-20">Pluggable IdRef</a>
 * </p>
 */
public class ScopedIdResolver extends IDResolver {
	private static class Key {
		private Class<?> klass;
		private String id;

		public Key(Class<?> klass, String id) {
			this.klass = klass;
			this.id = id;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Key other = (Key) obj;
			if (klass != other.getClass()
					&& (klass == null || !klass.equals(other.getKlass()))) {
				return false;
			}
			if ((id == null) ? (other.getId() != null) : !id.equals(other.getId())) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 29 * hash + (klass != null ? klass.hashCode() : 0);
			hash = 29 * hash + (id != null ? id.hashCode() : 0);
			return hash;
		}

		public String getId() {
			return id;
		}

		public Class<?> getKlass() {
			return klass;
		}
	}

	private static class DuplicateValidationEvent implements ValidationEvent {
		private String id;

		public DuplicateValidationEvent(String id) {
			this.id = id;
		}

		@Override
		public int getSeverity() {
			return ValidationEvent.FATAL_ERROR;
		}

		@Override
		public String getMessage() {
			return "Duplicate entry for id '" + id + "'";
		}

		@Override
		public Throwable getLinkedException() {
			return null;
		}

		@Override
		public ValidationEventLocator getLocator() {
			return new ValidationEventLocator() {
				@Override
				public URL getURL() {
					return null;
				}

				@Override
				public int getOffset() {
					return 0;
				}

				@Override
				public int getLineNumber() {
					return -1;
				}

				@Override
				public int getColumnNumber() {
					return -1;
				}

				@Override
				public Object getObject() {
					return null;
				}

				@Override
				public Node getNode() {
					return null;
				}
			};
		}
	}


	private Map<Key, Object> map = new HashMap<Key, Object>();
	private ValidationEventHandler eventHandler;

	@Override
	public void startDocument(ValidationEventHandler eventHandler) throws SAXException {
		super.startDocument(eventHandler);
		this.eventHandler = eventHandler;
	}


	@Override
	public void endDocument() throws SAXException {
		map.clear();
		eventHandler = null;

		super.endDocument();
	}

	@Override
	public void bind(final String id, final Object object) throws SAXException {
		Class<?> klass = object.getClass();
		while (klass != Object.class && klass != Configurable.class) {
			Key key = new Key(klass, id);
			if (map.containsKey(key)) {
				ValidationEvent event = new DuplicateValidationEvent(id);
				if (!eventHandler.handleEvent(event)) {
					throw new SAXException(event.getMessage());
				}
			}
			map.put(key, object);
			klass = klass.getSuperclass();
		}
	}

	@Override
	public Callable<?> resolve(final String id, @SuppressWarnings("rawtypes") final Class targetType) throws SAXException {
		return new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return map.get(new Key(targetType, id));
			}
		};
	}
}
