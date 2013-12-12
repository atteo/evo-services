/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.activiti;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Starts Activiti database
 */
@XmlRootElement(name = "activiti")
public class Activiti extends TopLevelService {
	@XmlDefaultValue("default")
	String name;

	/**
	 * True if to update db schema on boot time:
	 *
	 * can be false/true/create/create-drop
	 *
	 * @see org.activiti.engine.ProcessEngineConfiguration
	 * @see org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl
	 */
	@XmlDefaultValue("true")
	String dbSchemaUpdate;

	/**
	 * True if the job executor should be activated.
	 */
	@XmlDefaultValue("false")
	Boolean jobExecutorActivate;

	/**
	 * The host of the mail server
	 */
	String mailServerHost;

	/**
	 * The port of the mail server
	 */
	@XmlDefaultValue("25")
	Integer mailServerPort;

	/**
	 * History config
	 */
	@XmlDefaultValue("audit")
	String history;


	private class ProcessEngineProvider implements Provider<ProcessEngine> {
		@Inject
		Injector injector;

		@Inject
		private DataSource dataSource;

		@Override
		public ProcessEngine get() {
			ProcessEngine pe = JtaProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
					.setDatabaseSchemaUpdate(dbSchemaUpdate).setDataSource(dataSource)
					.setJobExecutorActivate(jobExecutorActivate).setHistory(history)
					.setMailServerHost(mailServerHost).setMailServerPort(mailServerPort).buildProcessEngine();

			ProcessEngines.registerProcessEngine(pe);

			return pe;
		}
	}

	@Override
	public Module configure() {
		return new Module() {
			@Override
			public void configure(Binder binder) {
				binder.bind(ProcessEngine.class).toProvider(new ProcessEngineProvider()).in(Singleton.class);
			}
		};
	}

	@Override
	public void start() {
		ProcessEngines.init();
	}

	@Override
	public void stop() {
		ProcessEngines.destroy();
	}
}
