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
package org.atteo.moonshine;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.services.TopLevelService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * Robot service.
 */
@XmlRootElement(name = "robot")
public class RobotService extends TopLevelService {
	@XmlIDREF
	@ImportService
	@Left
	private LegService leftLeg;

	@XmlIDREF
	@ImportService
	@Right
	private LegService rightLeg;

	@XmlIDREF
	@ImportService
	@Left
	private HandService leftHand;

	@XmlIDREF
	@ImportService
	@Right
	private HandService rightHand;

	@XmlIDREF
	@ImportService
	private HeadService head;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(Robot.class);
				bind(Boolean.class).annotatedWith(Names.named("headless")).toInstance(head == null);
			}
		};
	};
}
