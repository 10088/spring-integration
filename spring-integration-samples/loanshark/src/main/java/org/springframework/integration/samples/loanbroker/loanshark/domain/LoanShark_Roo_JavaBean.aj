package org.springframework.integration.samples.loanbroker.loanshark.domain;

import java.lang.Double;
import java.lang.Long;
import java.lang.String;

privileged aspect LoanShark_Roo_JavaBean {
    
    public String LoanShark.getName() {
        return this.name;
    }
    
    public void LoanShark.setName(String name) {
        this.name = name;
    }
    
    public Long LoanShark.getCounter() {
        return this.counter;
    }
    
    public void LoanShark.setCounter(Long counter) {
        this.counter = counter;
    }
    
    public Double LoanShark.getAverageRate() {
        return this.averageRate;
    }
    
    public void LoanShark.setAverageRate(Double averageRate) {
        this.averageRate = averageRate;
    }
    
}
