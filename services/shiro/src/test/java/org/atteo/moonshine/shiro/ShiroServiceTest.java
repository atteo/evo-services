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

import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.mgt.WebSecurityManager;
import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.atteo.moonshine.tests.ShiroRule;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <shiro>"
		+ "        <realms>"
		+ "            <in-place>"
		+ "                <accounts>"
		+ "                    <account username='admin' password='admin' administrator='true'>"
		+ "                        <roles>"
		+ "                            <role>admin</role>"
		+ "                        </roles>"
		+ "                    </account>"
		+ "                </accounts>"
		+ "            </in-place>"
		+ "        </realms>"
		+ "    </shiro>"
		+ "</config>")
public class ShiroServiceTest extends MoonshineTest {
	@Rule
	public ShiroRule shiro = new ShiroRule();

	@Test
	public void shouldLoginAdmin() {
		SecurityUtils.getSubject().login(new UsernamePasswordToken("admin", "admin"));
		assertTrue(SecurityUtils.getSubject().hasRole("admin"));
		assertTrue(SecurityUtils.getSubject().isPermitted("some_permission"));
	}

	@Test(expected = AuthenticationException.class)
	public void shouldCheckPassword() {
		SecurityUtils.getSubject().login(new UsernamePasswordToken("admin", "wrong password"));
	}

	@Inject
	private static WebSecurityManager manager;

	@Test
	public void shouldBindWebSecurityManager() {
		assertThat(manager).isNotNull();
	}
}
