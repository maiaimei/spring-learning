package cn.maiaimei.service;

import cn.maiaimei.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

  @Autowired
  private InnerTransactionService innerService;

  // 1. REQUIRED - 默认传播行为，如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
  @Transactional(propagation = Propagation.REQUIRED)
  public String testRequired() {
    TransactionUtils.debugTransactionStatus("外层方法 - REQUIRED");
    innerService.innerRequired();
    return "REQUIRED测试完成";
  }

  // 2. SUPPORTS - 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行
  @Transactional(propagation = Propagation.SUPPORTS)
  public String testSupports() {
    TransactionUtils.debugTransactionStatus("外层方法 - SUPPORTS");
    innerService.innerSupports();
    return "SUPPORTS测试完成";
  }

  // 3. MANDATORY - 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常
  @Transactional(propagation = Propagation.MANDATORY)
  public String testMandatory() {
    TransactionUtils.debugTransactionStatus("外层方法 - MANDATORY");
    innerService.innerMandatory();
    return "MANDATORY测试完成";
  }

  // 4. REQUIRES_NEW - 创建一个新的事务，如果当前存在事务，则把当前事务挂起
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String testRequiresNew() {
    TransactionUtils.debugTransactionStatus("外层方法 - REQUIRES_NEW");
    innerService.innerRequiresNew();
    return "REQUIRES_NEW测试完成";
  }

  // 5. NOT_SUPPORTED - 以非事务方式执行操作，如果当前存在事务，则把当前事务挂起
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public String testNotSupported() {
    TransactionUtils.debugTransactionStatus("外层方法 - NOT_SUPPORTED");
    innerService.innerNotSupported();
    return "NOT_SUPPORTED测试完成";
  }

  // 6. NEVER - 以非事务方式执行，如果当前存在事务，则抛出异常
  @Transactional(propagation = Propagation.NEVER)
  public String testNever() {
    TransactionUtils.debugTransactionStatus("外层方法 - NEVER");
    innerService.innerNever();
    return "NEVER测试完成";
  }

  // 7. NESTED - 如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行
  @Transactional(propagation = Propagation.NESTED)
  public String testNested() {
    TransactionUtils.debugTransactionStatus("外层方法 - NESTED");
    innerService.innerNested();
    return "NESTED测试完成";
  }

  // 演示不同传播行为的组合调用 - REQUIRED + NESTED
  @Transactional(propagation = Propagation.REQUIRED)
  public String testRequiredWithNested() {
    TransactionUtils.debugTransactionStatus("外层方法 - REQUIRED");
    innerService.innerNested();
    return "REQUIRED + NESTED 演示完成";
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public String testRequiredWithRequiresNew() {
    TransactionUtils.debugTransactionStatus("外层方法 - REQUIRED");
    innerService.innerRequiresNew();
    return "REQUIRED + REQUIRES_NEW 演示完成";
  }
}