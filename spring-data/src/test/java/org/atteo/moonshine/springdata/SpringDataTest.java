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
package org.atteo.moonshine.springdata;

import javax.inject.Inject;

import org.atteo.moonshine.jta.Transactional;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.google.common.collect.Iterables;

@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "    <atomikos/>"
		+ "    <transactional/>"
		+ "    <h2/>"
		+ "    <hibernate>"
		+ "        <initSchema>create</initSchema>"
		+ "    </hibernate>"
		+ "    <springData/>"
		+ "</config>")
public class SpringDataTest extends MoonshineTest {

	@Inject
	private UserRepository userRepository;

	@Test
	@Transactional
	public void simple() {
		User user = new User();
		user.setName("Nicolaus Copernicus");
		userRepository.save(user);
		Iterable<User> users = userRepository.findAll();
		assertEquals(1, Iterables.size(users));

		users = userRepository.findByName("Nicolaus Copernicus");
		assertEquals(1, Iterables.size(users));
	}
}
