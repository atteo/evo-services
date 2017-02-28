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

package org.atteo.moonshine.websocket.jsonmessages;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.google.common.base.Preconditions.checkState;

public class HandlerDispatcher {
	private final List<OnMessageMethodMetadata> onMessageMethods = new ArrayList<>();
	private final ObjectMapper encoderObjectMapper = new ObjectMapper();
	private final ObjectMapper decoderObjectMapper = new ObjectMapper();

	public <T> void addHandler(Class<T> klass, Provider<? extends T> provider) {
		for (Method method : klass.getMethods()) {
			if (method.isAnnotationPresent(OnMessage.class)) {
				registerOnMessageMethod(method, provider);
			}
		}
	}

	public <T> void addHandler(final T handler) {
		this.addHandler((Class<T>)handler.getClass(), () -> handler);
	}

	public <T> SenderProvider<T> addSender(Class<T> klass) {
		checkState(klass.isInterface(), "Provided Class object must represent an interface");

		for (Method method : klass.getMethods()) {
			registerSenderMethod(method);
		}
		@SuppressWarnings("unchecked")
		final Class<T> proxyClass = (Class<T>) Proxy.getProxyClass(Thread.currentThread().getContextClassLoader(),
				klass);

		class SenderInvocationHandler implements InvocationHandler {
			private final Session session;

			public SenderInvocationHandler(Session session) {
				this.session = session;
			}

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				String request = encoderObjectMapper.writeValueAsString(args[0]);
				session.getBasicRemote().sendText(request);
				return null;
			}
		}

		return (Session session) -> {
			try {
				return proxyClass.getConstructor(new Class<?>[] { InvocationHandler.class }).newInstance(
						new SenderInvocationHandler(session));
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		};
	}

	private void registerOnMessageMethod(Method method, Provider<?> provider) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new RuntimeException("Method marked with @" + OnMessage.class.getSimpleName() +
					" must have exactly one argument whose super class is " + JsonMessage.class.getSimpleName());
		}
		Class<?> parameterType = parameterTypes[0];
		if (!JsonMessage.class.isAssignableFrom(parameterType)) {
			throw new RuntimeException("Method marked with @" + OnMessage.class.getSimpleName() +
					" must have exactly one argument whose super class is " + JsonMessage.class.getSimpleName());
		}
		decoderObjectMapper.registerSubtypes(parameterType);
		Class<?> returnType = method.getReturnType();

		if (returnType != Void.TYPE) {
			encoderObjectMapper.registerSubtypes(returnType);
		}
		onMessageMethods.add(new OnMessageMethodMetadata(parameterType, provider, method));
	}

	private void registerSenderMethod(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new RuntimeException("Sender method" +
					" must have exactly one argument whose super class is " + JsonMessage.class.getSimpleName());
		}
		Class<?> parameterType = parameterTypes[0];
		if (!JsonMessage.class.isAssignableFrom(parameterType)) {
			throw new RuntimeException("Sender method" +
					" must have exactly one argument whose super class is " + JsonMessage.class.getSimpleName());
		}
		encoderObjectMapper.registerSubtypes(parameterType);
		Class<?> returnType = method.getReturnType();
		if (returnType != Void.TYPE) {
			throw new RuntimeException("Sender method must have " + Void.class.getSimpleName() + " return type");
		}
	}

	public String callOnMessage(String message) throws JsonProcessingException, IOException {
		JsonMessage request = decoderObjectMapper.readValue(message, JsonMessage.class);
		for (OnMessageMethodMetadata metadata : onMessageMethods) {
			if (metadata.getMessageType().isAssignableFrom(request.getClass())) {
				JsonMessage response = metadata.call(request);
				if (response == null) {
					return null;
				} else {
					return encoderObjectMapper.writeValueAsString(response);
				}
			}
		}
		throw new RuntimeException("Unknown message type: " + request.getClass().getName());
	}

	private static class OnMessageMethodMetadata {
		private final Provider<?> provider;
		private final Class<?> messageType;
		private final Method method;

		public OnMessageMethodMetadata(Class<?> messageType, Provider<?> provider, Method method) {
			super();
			this.provider = provider;
			this.messageType = messageType;
			this.method = method;
		}

		public Class<?> getMessageType() {
			return messageType;
		}

		public JsonMessage call(JsonMessage message) {
			try {
				Object handler = provider.get();
				return (JsonMessage) method.invoke(handler, message);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
