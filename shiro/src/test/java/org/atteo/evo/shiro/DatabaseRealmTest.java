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
package org.atteo.evo.shiro;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.atteo.evo.jta.Transactional;
import org.atteo.evo.migrations.Migrations;
import org.atteo.evo.tests.ServicesTest;
import org.atteo.evo.tests.ShiroRule;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.Inject;

public class DatabaseRealmTest extends ServicesTest {
	@Rule
	public ShiroRule shiro = new ShiroRule();

	@Inject
	private static Migrations migrations;

	@BeforeClass
	@Transactional
	public static void init() {
		migrations.migrate("fixtures/login-realm.xml");
	}
	
	@After
	public void tearDown() {
		SecurityUtils.getSubject().logout();
	}

	@Test
	@Transactional
	public void login() throws NotSupportedException, SystemException {
		SecurityUtils.getSubject().login(new UsernamePasswordToken("joey", "how you doin?"));
		assertTrue(SecurityUtils.getSubject().isAuthenticated());
	}

	@Test(expected = AuthenticationException.class)
	@Transactional
	public void wrongPassword() {
		SecurityUtils.getSubject().login(new UsernamePasswordToken("joey", "wrong password"));
	}

	@Test
	public void notLoggedIn() {
		assertFalse(SecurityUtils.getSubject().isAuthenticated());
	}
}
