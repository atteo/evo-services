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

import java.io.IOException;


/**
 * Default main() method implementation.
 */
public class Main {
	// just throw exceptions, Moonshine registers Thread.UncaughtExceptionHandler to handle them gracefully
	public static void main(final String[] args) throws IOException, MoonshineException {
		Moonshine moonshine = Moonshine.Factory.builder()
				.arguments(args)
				.build();
		if (moonshine != null) {
			moonshine.start();
		}
	}
}
