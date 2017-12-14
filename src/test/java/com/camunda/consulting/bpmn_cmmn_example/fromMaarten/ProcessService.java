package com.camunda.consulting.bpmn_cmmn_example.fromMaarten;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessService {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessService.class);
  
  public String getField(CaseExecution caseExecution, Object two, Object three) {
    LOGGER.info("get Field with {}, {}, {}", caseExecution, two, three);
    CaseExecutionEntity caseExecutionEntity = (CaseExecutionEntity) caseExecution;
    String weather = (String) caseExecutionEntity.getVariable("weather");
    LOGGER.info("weather is now: {}", weather);
    return weather;
  }

}
