package org.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InnerTransactionService {

    @Transactional(propagation = Propagation.REQUIRED)
    public void innerRequired() {
        System.out.println("内层方法 - REQUIRED");
        // 在此处设置断点观察事务状态
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void innerSupports() {
        System.out.println("内层方法 - SUPPORTS");
        // 在此处设置断点观察事务状态
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void innerMandatory() {
        System.out.println("内层方法 - MANDATORY");
        // 在此处设置断点观察事务状态
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void innerRequiresNew() {
        System.out.println("内层方法 - REQUIRES_NEW");
        // 在此处设置断点观察事务状态
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void innerNotSupported() {
        System.out.println("内层方法 - NOT_SUPPORTED");
        // 在此处设置断点观察事务状态
    }

    @Transactional(propagation = Propagation.NEVER)
    public void innerNever() {
        System.out.println("内层方法 - NEVER");
        // 在此处设置断点观察事务状态
    }

    @Transactional(propagation = Propagation.NESTED)
    public void innerNested() {
        System.out.println("内层方法 - NESTED");
        // 在此处设置断点观察事务状态
    }
}