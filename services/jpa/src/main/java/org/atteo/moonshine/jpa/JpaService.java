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
package org.atteo.moonshine.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.atteo.moonshine.database.DatabaseService;
import org.atteo.moonshine.jta.JtaService;
import org.atteo.moonshine.services.ImportService;
import org.atteo.moonshine.TopLevelService;

/**
 * Marks JPA implementation services.
 *
 * <p>
 * Should bind {@link EntityManagerFactory} and {@link EntityManager}.
 * </p>
 */
public abstract class JpaService extends TopLevelService {
	/**
	 * Specifies database to use.
	 */
	@XmlElement
	@XmlIDREF
	@ImportService
	protected DatabaseService database;

	/**
	 * Specifies JTA implementation to use.
	 */
	@XmlElement
	@XmlIDREF
	@ImportService
	private JtaService jta;


	/**
	 * Returns {@link DatabaseService} used by this JPA instance.
	 */
	public DatabaseService getDatabaseService() {
		return database;
	}
}
