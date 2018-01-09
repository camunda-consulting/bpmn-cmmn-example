package com.camunda.consulting.bpmn_cmmn_example.fromMaarten;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.*;

import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SetFollowUpDateTest {
  
  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Test
  @Deployment(resources = "follow_up_date_test.cmmn")
  public void testSetFollowUpdate() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("FollowUpDateCheckCase");
    
    assertThat(caseInstance).isActive();
    
    CaseExecution requestDocumentTask = caseExecution("request_documents_task", caseInstance);
    caseService().manuallyStartCaseExecution(requestDocumentTask.getId());
    
    caseService().completeCaseExecution(requestDocumentTask.getId());
    
    Task checkDocumentTask = taskQuery().taskDefinitionKey("check_documents_task").singleResult();
    assertThat(checkDocumentTask.getFollowUpDate()).isNotNull();
  }

}
