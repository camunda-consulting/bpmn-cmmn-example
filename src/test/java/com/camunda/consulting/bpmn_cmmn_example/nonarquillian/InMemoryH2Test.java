package com.camunda.consulting.bpmn_cmmn_example.nonarquillian;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camunda.consulting.bpmn_cmmn_example.CustomerValueBean;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test {

//  @ClassRule
  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();
//  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

  private static final String PROCESS_DEFINITION_KEY = "Bpmn-Cmmn-Example-Process";
  private static final Logger log = LoggerFactory.getLogger(InMemoryH2Test.class);

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  /**
   * Just tests if the process definition is deployable.
   */
  @Test
  @Deployment(resources = "process.bpmn")
  public void testParsingAndDeployment() {
    // nothing is done here, as we just want to check for exceptions during deployment
  }

  @Test
  @Deployment(resources = {"process.bpmn", "exampleCase.cmmn"})
  public void testHappyPath() {
	  ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
	  
	  // Now: Drive the process by API and assert correct behavior by camunda-bpm-assert
	  assertThat(processInstance).isWaitingAt("caseWorkingCallActivity");
	  
	  CaseInstance caseInstance = caseService().createCaseInstanceQuery().singleResult();
	  assertThat(caseInstance).isActive();
	  logStateOfAllElements(caseInstance);
	  CaseExecution fulfillFirstHumanTask = caseExecutionQuery().activityId("fulfillFirstHumanTask").singleResult();
	  complete(fulfillFirstHumanTask, withVariables("initSuccessful", true));
	  complete(caseExecution("initialStage", caseInstance));
//	  assertThat(caseExecutionQuery().activityId("initialStage").singleResult()).isTerminated();
	  logStateOfAllElements(caseInstance);
	  complete(caseExecution("repeatThisHumanTask", caseInstance), withVariables("orderValue", 101));
	  logStateOfAllElements(caseInstance);
	  logCaseHistory(caseInstance);
	  assertThat(processInstance).isEnded().hasPassed("endEventSuccessful");
  }

  @Test
  @Deployment(resources = "exampleCase.cmmn")
  public void testCaseAbortInitialStage() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("ExampleCase");

    CaseExecution fulfillFirstHumanTask = caseExecutionQuery().activityId("fulfillFirstHumanTask").singleResult();
    complete(fulfillFirstHumanTask, withVariables("initSuccessful", false));
    complete(caseExecution("initialStage", caseInstance));
    logStateOfAllElements(caseInstance);
    logCaseHistory(caseInstance);
    assertThat(caseInstance).isCompleted();
  }
  
  @Test
  @Deployment(resources = "exampleCase.cmmn")
  public void testCaseActivateTask() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("ExampleCase");
    
    CaseExecution manualActivateHumanTask = caseExecution("manualActivateHumanTask", caseInstance);
    caseService().manuallyStartCaseExecution(manualActivateHumanTask.getId());
    assertThat(caseExecution("manualActivateHumanTask", caseInstance)).isActive();
    complete(manualActivateHumanTask, withVariables("initSuccessful", true));
//    assertThat(caseExecution("initialStage", caseInstance)).isCompleted();
    logStateOfAllElements(caseInstance);
    logCaseHistory(caseInstance);
    HistoricCaseActivityInstance historicInitialStage = historyService().createHistoricCaseActivityInstanceQuery().caseActivityIdIn("initialStage").singleResult();
    assertThat(historicInitialStage.isCompleted()).isTrue();
    logStateOfAllElements(caseInstance);
    complete(caseExecution("repeatThisHumanTask", caseInstance), withVariables("orderValue", 101));
    assertThat(caseInstance).isCompleted();
  }
  
  @Test
  @Deployment(resources = "exampleCase.cmmn")
  public void testCaseCompleteActiveTaskFirst() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("ExampleCase");
    
    CaseExecution manualActivateHumanTask = caseExecution("manualActivateHumanTask", caseInstance);
    caseService().manuallyStartCaseExecution(manualActivateHumanTask.getId());
    assertThat(caseExecution("manualActivateHumanTask", caseInstance)).isActive();
    complete(caseExecution("fulfillFirstHumanTask", caseInstance));
    logStateOfAllElements(caseInstance);
    complete(manualActivateHumanTask, withVariables("initSuccessful", true));
    logStateOfAllElements(caseInstance);
    logCaseHistory(caseInstance);
    HistoricCaseActivityInstance historicInitialStage = historyService().createHistoricCaseActivityInstanceQuery().caseActivityIdIn("initialStage").singleResult();
    assertThat(historicInitialStage.isCompleted()).isTrue();
    logStateOfAllElements(caseInstance);
    complete(caseExecution("repeatThisHumanTask", caseInstance), withVariables("orderValue", 101));
    assertThat(caseInstance).isCompleted();
  }
  
  @Test
  @Deployment(resources = "exampleCase.cmmn")
  public void testCaseRepetition() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("ExampleCase");
    
    complete(caseExecution("fulfillFirstHumanTask", caseInstance), withVariables("initSuccessful", true));
    complete(caseExecution("initialStage", caseInstance));
    assertThat(caseExecution("repeatThisHumanTask", caseInstance)).isActive();
  }
  
  @Test
  @Deployment(resources = "exampleCase.cmmn")
  public void testEntryCriterionCustomerValueBigEnough() {
    CustomerValueBean customerValueBean = new CustomerValueBean();
    Mocks.register("customerValueBean", customerValueBean);
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("ExampleCase");
    completeFirstStageSuccessful(caseInstance);
    
    assertThat(caseExecution("conditionalActivateHumanTask", caseInstance)).isAvailable();
    
    customerValueBean.setCustomerValue(1001);
    caseService().setVariable(caseInstance.getCaseInstanceId(), "customerValueChanged", true);
    
    assertThat(caseExecution("conditionalActivateHumanTask", caseInstance)).isActive();
  }

  @Test
  @Deployment(resources = "exampleCase.cmmn")
  public void testEntryCriterionCustomerValueSet() {
    CustomerValueBean customerValueBean = new CustomerValueBean();
    Mocks.register("customerValueBean", customerValueBean);
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("ExampleCase");
    completeFirstStageSuccessful(caseInstance);

    customerValueBean.setCustomerValue(20);
    caseService().setVariable(caseInstance.getCaseInstanceId(), "customerValueChanged", true);    
    assertThat(caseExecution("conditionalActivateHumanTask", caseInstance)).isAvailable();
    
    customerValueBean.setCustomerValue(1001);
    caseService().setVariable(caseInstance.getCaseInstanceId(), "customerValueChanged", true);
    
    assertThat(caseExecution("conditionalActivateHumanTask", caseInstance)).isActive();
  }

  private void completeFirstStageSuccessful(CaseInstance caseInstance) {
    complete(caseExecution("fulfillFirstHumanTask", caseInstance), withVariables("initSuccessful", true));
    complete(caseExecution("initialStage", caseInstance));
  }

  private void logStateOfAllElements(CaseInstance caseInstance) {
    List<CaseExecution> executionList = caseService().createCaseExecutionQuery().caseInstanceId(caseInstance.getId()).list();
    log.debug("All Elements of caseinstance {}", caseInstance.getId());
    for (CaseExecution caseExecution : executionList) {
      log.debug("id: {}, key: {}, name: {}, type: {}, available: {}, active: {}, enabled: {}, diabled: {}, required: {}, terminated: {}", 
          caseExecution.getId(),
          caseExecution.getActivityId(),
          caseExecution.getActivityName(),
          caseExecution.getActivityType(),
          caseExecution.isAvailable(),
          caseExecution.isActive(),
          caseExecution.isEnabled(),
          caseExecution.isDisabled(),
          caseExecution.isRequired(),
          caseExecution.isTerminated()
          );
    }
  }
  
  private void logCaseHistory(CaseInstance caseInstance) {
    List<HistoricCaseActivityInstance> activityList = historyService().createHistoricCaseActivityInstanceQuery().list();
    log.debug("All historic elements of caseinstance {}", caseInstance.getId());
    for (HistoricCaseActivityInstance activityInstance : activityList) {
      log.debug("id: {}, name: {}, completed: {}, terminated: {}, available: {}", 
          activityInstance.getCaseActivityId(),
          activityInstance.getCaseActivityName(),
          activityInstance.isCompleted(),
          activityInstance.isTerminated(),
          activityInstance.isAvailable()
          );
    }
  }

}
