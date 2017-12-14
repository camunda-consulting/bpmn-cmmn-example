package com.skryv.backend.bpm.tasks;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ExitCriterionListener implements CaseExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ExitCriterionListener.class);

    @Override
    public void notify(DelegateCaseExecution delegateCaseExecution) throws Exception {
        String myId = delegateCaseExecution.getId();
        String myParentId = delegateCaseExecution.getParentId();
        log.info("My id: {}, parent id: {}", myId, myParentId);
        
        CaseService caseService = delegateCaseExecution.getProcessEngineServices().getCaseService();
        List<CaseExecution> caseExecutions = caseService.createCaseExecutionQuery().caseInstanceId(delegateCaseExecution.getCaseInstanceId()).list();
        logCaseExections(caseExecutions);
        
        List<CaseExecution> mySiblings = caseExecutions.stream().filter(caseExecution -> 
          caseExecution.getParentId() != null &&
          caseExecution.getParentId().equals(myParentId) && 
          caseExecution.getId() != myId).collect(Collectors.toList());
        mySiblings.forEach(sibling -> log.info("sibling id: {}, actid: {}, name: {}", sibling.getId(), sibling.getActivityId(), sibling.getActivityName()));
        mySiblings.forEach(sibling -> terminateExecution((DelegateCaseExecution) sibling, caseExecutions));
        

//        for (CaseExecution caseExecution : caseExecutions) {
//            if (!caseExecution.getId().equals(delegateCaseExecution.getId()) &&
//                    caseExecution.getParentId() != null &&
//                    caseExecution.getParentId().equals(delegateCaseExecution.getParentId())) {
//                log.info("terminate other caseExecution {}, {}", caseExecution.getId(), caseExecution.getActivityName());
//                if (caseExecution.isActive()
//                        && !((CaseInstance) caseExecution).isCompleted()
//                        && !caseExecution.isTerminated()
//                        && caseExecution.getActivityName() != null) {
//                    caseService.terminateCaseExecution(caseExecution.getId());
//                }
//            } else {
//                log.info("nothing to do with {}, {}", caseExecution.getId(), caseExecution.getActivityName());
//            }
//        }
        log.info("ExitCriterionListener ended");
    }

    private void terminateExecution(DelegateCaseExecution caseExecution, List<CaseExecution> caseExecutions) {
        List<CaseExecution> children = findChildren(caseExecution, caseExecutions);
        if (children.size() > 0) {
          log.info("terminate all children of {}", caseExecution);
          children.forEach(child -> terminateExecution((DelegateCaseExecution) child, caseExecutions));
        } else { 
          log.info("check termination for {}: completed: {}, terminated: {}", caseExecution.getId(), caseExecution.isCompleted(), caseExecution.isTerminated());
          if (caseExecution.isActive()) {
//            if (!caseExecution.isCompleted() && !caseExecution.isTerminated()) {
            log.info("terminate {} now", caseExecution.getId());
            caseExecution.getProcessEngineServices().getCaseService().terminateCaseExecution(caseExecution.getId());
            log.info("executions after termination:");
            logCaseExections(caseExecution.getProcessEngineServices().getCaseService().createCaseExecutionQuery().list());
          } else {
            log.info("don't terminate {}", caseExecution.getId());
          }
        }
    }
    
    private List<CaseExecution> findChildren(DelegateCaseExecution caseExecution, List<CaseExecution> caseExecutions) {
      log.info("search for children of {}", caseExecution.getId());
      return caseExecutions.stream().filter(possibleChild -> 
          possibleChild.getParentId() != null && 
          possibleChild.getParentId().equals(caseExecution.getId())).collect(Collectors.toList());
    }
      

    private void logCaseExections(List<CaseExecution> caseExecutions) {
        for (CaseExecution caseExecution : caseExecutions) {
            CaseExecutionEntity entity = (CaseExecutionEntity) caseExecution;
            log.info("case execution entity {}, {}, id: {}, parent: {}, active: {}, available: {}, completed: {}, disabled: {}, enabled: {}, required: {}, terminated: {}, state: {}",
                    entity.getActivityType(),
                    entity.getActivityName(),
                    entity.getId(),
                    entity.getParentId(),
                    entity.isActive(),
                    entity.isAvailable(),
                    entity.isCompleted(),
                    entity.isDisabled(),
                    entity.isEnabled(),
                    entity.isRequired(),
                    entity.isTerminated(),
                    entity.getState());
        }
    }
}