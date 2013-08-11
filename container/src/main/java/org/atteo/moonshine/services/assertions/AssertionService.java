/*
 * Copyright 2011 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.services.assertions;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.injection.InjectMembers;
import org.atteo.moonshine.services.TopLevelService;

/**
 * Service which can be used to test for some conditions.
 *
 * <p>
 * The purpose of this service is to help you debug your configuration
 * file.
 * </p>
 *
 * <p>
 * Most of the {@link Assertion assertions} will fail the application
 * startup when their requirement is not satisfied. There is also
 * {@link EchoAssertion} which prints the specified content.
 * </p>
 */
@XmlRootElement(name = "assertions")
public class AssertionService extends TopLevelService {
	@InjectMembers
	@XmlElementRef
	private List<Assertion> assertions;

	@Override
	public void start() {
		if (assertions == null) {
			return;
		}

		for (Assertion assertion : assertions) {
			assertion.check();
		}
	}
}
