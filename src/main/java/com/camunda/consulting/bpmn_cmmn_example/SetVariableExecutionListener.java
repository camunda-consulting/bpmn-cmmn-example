package com.camunda.consulting.bpmn_cmmn_example;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVariableExecutionListener implements CaseExecutionListener {
  
  private static final Logger log = LoggerFactory.getLogger(SetVariableExecutionListener.class);

  @Override
  public void notify(DelegateCaseExecution execution) throws Exception {
    log.info("listener called for event {}", execution.getEventName());
    execution.setVariableLocal("definition", "MyDocumentType");
    execution.setVariableLocal("scope", "DOSSIER");
    execution.setVariableLocal("markReadOnly", "true");
    execution.setVariableLocal("submitInvalidDocumentsAllowed", "true");
    execution.setVariableLocal("submitText", "This is my document!");
    execution.setVariableLocal("resultVariable", "MyResultVariable");
  }

}
