package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TransactionDebugService {

  @Autowired
  private ApplicationContext applicationContext;

  private TransactionDebugService getSelf() {
    return applicationContext.getBean(TransactionDebugService.class);
  }

  // 用于调试的方法，可以在断点处查看事务状态
  public void debugTransactionStatus(String methodName) {
    boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
    String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
    boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

    System.out.println("=== 事务状态调试 ===");
    System.out.println("方法: " + methodName);
    System.out.println("事务是否激活: " + isActive);
    System.out.println("事务名称: " + transactionName);
    System.out.println("是否只读: " + isReadOnly);
    System.out.println("==================");

    // 在此处设置断点，可以查看详细的事务状态
  }

  // 演示不同传播行为的组合调用
  @Transactional(propagation = Propagation.REQUIRED)
  public String demonstrateRequiredWithNested() {
    debugTransactionStatus("外层REQUIRED");
    getSelf().nestedMethod();
    return "REQUIRED + NESTED 演示完成";
  }

  @Transactional(propagation = Propagation.NESTED)
  public void nestedMethod() {
    debugTransactionStatus("内层NESTED");
    // 可以在这里模拟异常来观察嵌套事务的回滚行为
    // throw new RuntimeException("测试嵌套事务回滚");
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public String demonstrateRequiredWithRequiresNew() {
    debugTransactionStatus("外层REQUIRED");
    getSelf().requiresNewMethod();
    return "REQUIRED + REQUIRES_NEW 演示完成";
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void requiresNewMethod() {
    debugTransactionStatus("内层REQUIRES_NEW");
    // 可以在这里模拟异常来观察新事务的独立性
    // throw new RuntimeException("测试新事务独立性");
  }
}