package com.camunda.consulting.bpmn_cmmn_example;

import java.util.List;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompleteStageListener implements TaskListener {
  
  private static final Logger log = LoggerFactory.getLogger(CompleteStageListener.class);

  @Override
  public void notify(DelegateTask delegateTask) {
    log.info("task listener {}, event {}", delegateTask.getTaskDefinitionKey(), delegateTask.getEventName());
    if (delegateTask.getEventName().equals(EVENTNAME_COMPLETE)) {
      CaseService caseService = delegateTask.getCaseExecution().getProcessEngineServices().getCaseService();
      List<CaseExecution> caseExecutions = caseService.createCaseExecutionQuery().caseInstanceId(delegateTask.getCaseInstanceId()).list();
      logCaseExections(caseExecutions);
      
      for (CaseExecution caseExecution : caseExecutions) {
        if (caseExecution.getId().equals(delegateTask.getCaseExecutionId()) == false &&
            caseExecution.getParentId() != null &&
            caseExecution.getParentId().equals(delegateTask.getCaseExecution().getParentId())) {
          log.info("terminate other caseExecution {}, {}", caseExecution.getId(), caseExecution.getActivityName());
          caseService.terminateCaseExecution(caseExecution.getId());
        } else {
          log.info("nothing to do with {}, {}", caseExecution.getId(), caseExecution.getActivityName());
        }
      }
    }
  }

  private void logCaseExections(List<CaseExecution> caseExecutions) {
    for (CaseExecution caseExecution : caseExecutions) {
      log.info("case execution {}, {}, id: {}, parent: {}", 
          caseExecution.getActivityType(), 
          caseExecution.getActivityName(), 
          caseExecution.getId(), 
          caseExecution.getParentId());
    }
  }

}
