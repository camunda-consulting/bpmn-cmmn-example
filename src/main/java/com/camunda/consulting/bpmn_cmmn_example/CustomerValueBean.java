package com.camunda.consulting.bpmn_cmmn_example;

public class CustomerValueBean {
  
  private int customerValue = 0;
  
  public CustomerValueBean() {
    
  }

  public int getCustomerValue() {
    return customerValue;
  }

  public void setCustomerValue(int customerValue) {
    this.customerValue = customerValue;
  }
  
  public boolean isValueHighEnough() {
    return customerValue > 1000 ? true : false;
  }

}
