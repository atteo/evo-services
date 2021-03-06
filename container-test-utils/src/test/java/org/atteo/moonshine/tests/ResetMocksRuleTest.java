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
package org.atteo.moonshine.tests;

import javax.inject.Inject;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.Mockito;

public class ResetMocksRuleTest {
	@ClassRule
	public static MoonshineRule moonshine = new MoonshineRule();

	@Rule
	public MethodRule injectionRule = moonshine.injectMembers(this);

	@Rule
	public ResetMocksRule resetMocks = new ResetMocksRule(moonshine);

	@MockAndBind
	@Inject
	private Runnable runnable;

	@Test
	public void shouldWorkOnce() {
		runnable.run();

		Mockito.verify(runnable).run();
	}

	@Test
	public void shouldWorkSecondTimeAlso() {
		runnable.run();

		Mockito.verify(runnable).run();
	}
}
