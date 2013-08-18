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

import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlRootElement;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.atteo.moonshine.services.TopLevelService;
import org.perf4j.aop.AbstractJoinPoint;
import org.perf4j.aop.AgnosticTimingAspect;
import org.perf4j.aop.Profiled;
import org.perf4j.slf4j.Slf4JStopWatch;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

/**
 * Adds support for {@link Profiled} annotation.
 */
@Singleton
@XmlRootElement(name = "perf4j")
public class Perf4JService extends TopLevelService {
	private final AgnosticTimingAspect helper = new AgnosticTimingAspect();

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Profiled.class), new MethodInterceptor() {
					@Override
					public Object invoke(final MethodInvocation invocation) throws Throwable {
						Profiled annotation = invocation.getMethod().getAnnotation(Profiled.class);
						return helper.runProfiledMethod(new AbstractJoinPoint() {
							@Override
							public Object proceed() throws Throwable {
								return invocation.proceed();
							}

							@Override
							public Object getExecutingObject() {
								return invocation.getThis();
							}

							@Override
							public Object[] getParameters() {
								return invocation.getArguments();
							}

							@Override
							public String getMethodName() {
								return invocation.getMethod().getName();
							}
						}, annotation, new Slf4JStopWatch());
					}
				});
			}
		};
	}
}
