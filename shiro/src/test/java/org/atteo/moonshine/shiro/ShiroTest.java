/*
 * Copyright 2012 Atteo.
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
package org.atteo.moonshine.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.atteo.moonshine.tests.ServicesTest;
import org.atteo.moonshine.tests.ShiroRule;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;

public class ShiroTest extends ServicesTest {
	@Rule
	public ShiroRule shiro = new ShiroRule();

	@Test
	public void trivial() {
	}

	@Test
	public void login() {
		SecurityUtils.getSubject().login(new UsernamePasswordToken("admin", "admin"));
		assertTrue(SecurityUtils.getSubject().hasRole("admin"));
		assertTrue(SecurityUtils.getSubject().isPermitted("some_permission"));
	}

	@Test(expected = AuthenticationException.class)
	public void failedLogin() {
		SecurityUtils.getSubject().login(new UsernamePasswordToken("admin", "wrong password"));
	}
}
