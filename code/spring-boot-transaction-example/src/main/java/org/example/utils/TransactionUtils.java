package org.example.utils;

import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class TransactionUtils {

  // 用于调试的方法，可以在断点处查看事务状态
  public static void debugTransactionStatus(String methodName) {
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
}
