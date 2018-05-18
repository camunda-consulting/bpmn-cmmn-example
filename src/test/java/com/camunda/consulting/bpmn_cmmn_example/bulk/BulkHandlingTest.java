package com.camunda.consulting.bpmn_cmmn_example.bulk;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class BulkHandlingTest {
  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Test
  @Deployment(resources = "BulkLetterHandlingProcesses.bpmn")
  public void testDynamicMessages() {
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("normalDossier", 
        withVariables(
            "dynamicMessageActionStarted", "messageActionStarted", 
            "dynamicMessageActionCompleted", "messageActionCompleted"));
    assertThat(processInstance).isWaitingFor("messageActionStarted");
  }
}
