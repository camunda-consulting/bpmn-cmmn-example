package com.camunda.consulting.bpmn_cmmn_example.fromMaarten;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.withVariables;

import java.util.List;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camunda.consulting.bpmn_cmmn_example.nonarquillian.AutocompleteTest;

public class TerminateTest {
  // @ClassRule
  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();
  // public static ProcessEngineRule rule =
  // TestCoverageProcessEngineRuleBuilder.create().build();

  private static final Logger log = LoggerFactory.getLogger(AutocompleteTest.class);

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
    Mocks.register("processService", new ProcessService());
  }

  @Test
  @Deployment(resources = "cmmn_from_bpmn.cmmn")
  public void testWithCompletedLetMeKnowTheWheaterWithoutRain() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("cmmn_from_bpmn");
    
    CaseExecution chooseWheaterTask = caseExecution("task_choose_weather", caseInstance);
    complete(chooseWheaterTask, withVariables("weather", "no rain"));
    
    logStateOfAllElements(caseInstance);
    
    CaseExecution terminateCaseTask = caseExecution("task_terminate_case", caseInstance);
    
    log.info("Before manual start");
    caseService().manuallyStartCaseExecution(terminateCaseTask.getId());
    log.info("after manual start");
    logStateOfAllElements(caseInstance);
    
    assertThat(terminateCaseTask).isActive();
    complete(terminateCaseTask, withVariables("initSuccessful", true));
    
    logCaseHistory(caseInstance);
    
    assertThat(caseInstance).isCompleted();
  }
  
  // test with active (let me know the weather)
  @Test
  @Deployment(resources = "cmmn_from_bpmn.cmmn")
  public void testTerminateASAP() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("cmmn_from_bpmn");
    
    logStateOfAllElements(caseInstance);
    
    CaseExecution terminateCaseTask = caseExecution("task_terminate_case", caseInstance);
    
    log.info("Before manual start");
    caseService().manuallyStartCaseExecution(terminateCaseTask.getId());
    log.info("after manual start");
    logStateOfAllElements(caseInstance);
    
    assertThat(terminateCaseTask).isActive();
    complete(terminateCaseTask, withVariables("initSuccessful", true));
        
    logCaseHistory(caseInstance);
    
    // stage outer still active
    // stage inner still available
    assertThat(caseInstance).isCompleted();
  }
  
  // Test with active (buy raincover) (with Rain)
  @Test
  @Deployment(resources = "cmmn_from_bpmn.cmmn")
  public void testTerminateDuringBuyRaincover() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("cmmn_from_bpmn");
    
    CaseExecution chooseWheaterTask = caseExecution("task_choose_weather", caseInstance);
    complete(chooseWheaterTask, withVariables("weather", "rain"));
    
    CaseExecution buyRaincoverTask = caseExecution("task_buy_raincover", caseInstance);
    assertThat(buyRaincoverTask).isActive();
    
    logStateOfAllElements(caseInstance);
    
    CaseExecution terminateCaseTask = caseExecution("task_terminate_case", caseInstance);
    
    log.info("Before manual start");
    caseService().manuallyStartCaseExecution(terminateCaseTask.getId());
    log.info("after manual start");
    logStateOfAllElements(caseInstance);
    
    assertThat(terminateCaseTask).isActive();
    complete(terminateCaseTask, withVariables("initSuccessful", true));
    
    logCaseHistory(caseInstance);
    
    assertThat(caseInstance).isCompleted();
  }
  
  // Test with complete(buy raincover) and complete(communicationTask) (with Rain)
  @Test
  @Deployment(resources = "cmmn_from_bpmn.cmmn")
  public void testTerminateDuringBuyRaincoverAndCommunication() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("cmmn_from_bpmn");
    
    CaseExecution chooseWheaterTask = caseExecution("task_choose_weather", caseInstance);
    complete(chooseWheaterTask, withVariables("weather", "rain"));
    
    CaseExecution buyRaincoverTask = caseExecution("task_buy_raincover", caseInstance);
    assertThat(buyRaincoverTask).isActive();
    
    CaseExecution communicationTask = caseExecution("task_communication", caseInstance);
    assertThat(communicationTask).isEnabled();
    
    caseService().manuallyStartCaseExecution(communicationTask.getId());
    assertThat(communicationTask).isActive();
    
    logStateOfAllElements(caseInstance);
    
    CaseExecution terminateCaseTask = caseExecution("task_terminate_case", caseInstance);
    
    log.info("Before manual start");
    caseService().manuallyStartCaseExecution(terminateCaseTask.getId());
    log.info("after manual start");
    logStateOfAllElements(caseInstance);
    
    assertThat(terminateCaseTask).isActive();
    complete(terminateCaseTask, withVariables("initSuccessful", true));
    
    logCaseHistory(caseInstance);
    
    assertThat(caseInstance).isCompleted();
  }

  private void logStateOfAllElements(CaseInstance caseInstance) {
    List<CaseExecution> executionList = caseService().createCaseExecutionQuery().caseInstanceId(caseInstance.getId()).list();
    log.debug("All Elements of caseinstance {}", caseInstance.getId());
    for (CaseExecution caseExecution : executionList) {
      log.debug("id: {}, parent: {}, key: {}, name: {}, type: {}, available: {}, active: {}, enabled: {}, diabled: {}, required: {}, terminated: {}", caseExecution.getId(),
          caseExecution.getParentId(), caseExecution.getActivityId(), caseExecution.getActivityName(), caseExecution.getActivityType(), caseExecution.isAvailable(),
          caseExecution.isActive(), caseExecution.isEnabled(), caseExecution.isDisabled(), caseExecution.isRequired(), caseExecution.isTerminated());
    }
  }

  private void logCaseHistory(CaseInstance caseInstance) {
    List<HistoricCaseActivityInstance> activityList = historyService().createHistoricCaseActivityInstanceQuery().list();
    log.debug("All historic elements of caseinstance {}", caseInstance.getId());
    for (HistoricCaseActivityInstance activityInstance : activityList) {
      log.debug("id: {}, name: {}, completed: {}, terminated: {}, available: {}, active: {}", activityInstance.getCaseActivityId(), activityInstance.getCaseActivityName(),
          activityInstance.isCompleted(), activityInstance.isTerminated(), activityInstance.isAvailable(), activityInstance.isActive());
    }
  }

}
