package com.camunda.consulting.bpmn_cmmn_example.fromMaarten;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetFollowUpDateOnAnotherTaskListener implements CaseExecutionListener {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(SetFollowUpDateOnAnotherTaskListener.class);
  
  private FixedValue numberOfDays;

  @Override
  public void notify(DelegateCaseExecution caseExecution) {
    Task checkDocumentsTask = caseExecution
        .getProcessEngineServices()
        .getTaskService()
        .createTaskQuery()
        .taskDefinitionKey("check_documents_task")
        .singleResult();
    
    LOGGER.info("found task {}", checkDocumentsTask.getId());
    
    Date followUpDate = Date.from(Instant.now().plus(Long.valueOf((String) numberOfDays.getValue(caseExecution)), ChronoUnit.DAYS));
    
    LOGGER.info("set follow up date to {}", followUpDate);
    checkDocumentsTask.setFollowUpDate(followUpDate );
  }

}
