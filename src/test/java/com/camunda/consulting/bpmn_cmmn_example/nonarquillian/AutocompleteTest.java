package com.camunda.consulting.bpmn_cmmn_example.nonarquillian;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
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
public class AutocompleteTest {

//  @ClassRule
  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();
//  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

  private static final Logger log = LoggerFactory.getLogger(AutocompleteTest.class);

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
    Mocks.register("customerValueBean", new CustomerValueBean());
  }
  
  @Test
  @Deployment(resources = "Autocomplete.cmmn")
  public void testAutocomplete1() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("AutoCompleteCase");
    
    complete(caseExecution("alwaysHumanTask", caseInstance));
    
    logStateOfAllElements(caseInstance);
    
    logCaseHistory(caseInstance);
    
    assertThat(caseInstance).isCompleted();
  }

  @Test
  @Deployment(resources = "Autocomplete.cmmn")
  public void testAutocompleteWithConditional() {
    CaseInstance caseInstance = caseService().createCaseInstanceByKey("AutoCompleteCase");

    assertThat(caseExecution("conditionalHumanTask", caseInstance)).isAvailable();
    
    caseService().setVariable(caseInstance.getCaseInstanceId(), "myVar", "some value, not null");
    
    assertThat(caseExecution("conditionalHumanTask", caseInstance)).isActive();
    
    complete(caseExecution("conditionalHumanTask", caseInstance));
    complete(caseExecution("alwaysHumanTask", caseInstance));
    
    assertThat(caseInstance).isCompleted();
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
