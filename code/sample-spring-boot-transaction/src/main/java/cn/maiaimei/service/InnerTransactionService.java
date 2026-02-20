package cn.maiaimei.service;

import cn.maiaimei.utils.TransactionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InnerTransactionService {

  @Transactional(propagation = Propagation.REQUIRED)
  public void innerRequired() {
    TransactionUtils.debugTransactionStatus("内层方法 - REQUIRED");
    // 在此处设置断点观察事务状态
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public void innerSupports() {
    TransactionUtils.debugTransactionStatus("内层方法 - SUPPORTS");
    // 在此处设置断点观察事务状态
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void innerMandatory() {
    TransactionUtils.debugTransactionStatus("内层方法 - MANDATORY");
    // 在此处设置断点观察事务状态
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void innerRequiresNew() {
    TransactionUtils.debugTransactionStatus("内层方法 - REQUIRES_NEW");
    // 在此处设置断点观察事务状态

    // 可以在这里模拟异常来观察新事务的独立性
    // throw new RuntimeException("测试新事务独立性");
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void innerNotSupported() {
    TransactionUtils.debugTransactionStatus("内层方法 - NOT_SUPPORTED");
    // 在此处设置断点观察事务状态
  }

  @Transactional(propagation = Propagation.NEVER)
  public void innerNever() {
    TransactionUtils.debugTransactionStatus("内层方法 - NEVER");
    // 在此处设置断点观察事务状态
  }

  @Transactional(propagation = Propagation.NESTED)
  public void innerNested() {
    TransactionUtils.debugTransactionStatus("内层方法 - NESTED");
    // 在此处设置断点观察事务状态

    // 可以在这里模拟异常来观察嵌套事务的回滚行为
    // throw new RuntimeException("测试嵌套事务回滚");
  }
}