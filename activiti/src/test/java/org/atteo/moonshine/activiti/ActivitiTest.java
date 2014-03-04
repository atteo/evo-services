/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
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
package org.atteo.moonshine.activiti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.inject.Inject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActivitiTest extends MoonshineTest {
	@Inject
	ProcessEngine processEngine;

    @Test
    public void firstVvacationRequestFlowTest() {
        assertThat(processEngine).isNotNull();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        long processCounter = repositoryService.createProcessDefinitionQuery().count();
        repositoryService.createDeployment().addClasspathResource("vacation_request-bpmn20.xml").deploy();
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(processCounter+1);

		Map<String, Object> variables = new HashMap<>();
		variables.put("employeeName", "Kermit");
		variables.put("numberOfDays", new Integer(4));
		variables.put("vacationMotivation", "I'm really tired!");
		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacationRequest", variables);

		// Fetch all tasks for the management group
		TaskService taskService = processEngine.getTaskService();
		List<Task> mTasks = taskService.createTaskQuery().taskCandidateGroup("management").list();
		List<Task> kermitTasks = taskService.createTaskQuery().taskAssignee("Kermit").list();
		assertThat(mTasks).hasSize(1);
		assertThat(kermitTasks).hasSize(0);

		for (Task task : mTasks) {
			assertThat(task.getName()).isEqualTo("Handle vacation request");
		}

		// signal the task
		Task task = mTasks.get(0);
		Map<String, Object> taskVariables = new HashMap<>();
		taskVariables.put("vacationApproved", "false");
		taskVariables.put("managerMotivation", "We have a tight deadline!");
		taskService.complete(task.getId(), taskVariables);

		kermitTasks = taskService.createTaskQuery().taskAssignee("Kermit").list();
		assertThat(kermitTasks).hasSize(1);

		for (Task kt : kermitTasks) {
			assertThat(kt.getName()).isEqualTo("Adjust vacation request");
		}

		// process should exist because it's still active
		assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
				.singleResult()).isNotNull();
		// and exist in historic too
		HistoryService historicService = processEngine.getHistoryService();
		assertThat(historicService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId())
				.singleResult()).isNotNull();
		assertThat(historicService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId())
				.singleResult().getEndTime()).isNull();

		assertThat(processInstance.isEnded()).isEqualTo(false);
		task = kermitTasks.get(0);
		taskVariables = new HashMap<>();
		taskVariables.put("resendRequest", "false");
		taskService.complete(task.getId(), taskVariables);

		// process should not exist because it's not active
		assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).suspended()
				.singleResult()).isNull();

		// process should exist in historic service
		assertThat(historicService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId())
				.singleResult()).isNotNull();
		assertThat(historicService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId())
				.singleResult().getEndTime()).isNotNull();
	}

    @Test
    public void secondShouldParseHandlerBeInvoked() {
        assertThat(BpmnParseHandlerTest.counter).isEqualTo(2);
    }
}