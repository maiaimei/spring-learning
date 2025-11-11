package org.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Slf4j
@ContextConfiguration(classes = {TestApplication.class})
@ExtendWith(SpringExtension.class)
public class TransactionManagerTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @BeforeEach
  void setUp() {
    // 清理测试数据
    jdbcTemplate.execute("DELETE FROM books WHERE title LIKE '测试%'");
  }

  @Test
  void testInsertNonTransactionFailed() {
    String sql = "INSERT INTO books (title) VALUES (?)";
    jdbcTemplate.update(sql, "测试图书1");
    jdbcTemplate.update(sql, "测试图书2");
    jdbcTemplate.update(sql, (String) null); // 错误数据
    jdbcTemplate.update(sql, "测试图书4");
  }

  @Test
  void testInsertHasTransactionFailed() {
    String sql = "INSERT INTO books (title) VALUES (?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    log.info("开启事务完成");
    try {
      jdbcTemplate.update(sql, "测试图书1");
      jdbcTemplate.update(sql, "测试图书2");
      jdbcTemplate.update(sql, (String) null); // 错误数据
      jdbcTemplate.update(sql, "测试图书4");
      // 提交事务
      log.info("提交事务");
      transactionManager.commit(transactionStatus);
      log.info("提交事务完成");
    } catch (Exception ex) {
      // 回滚事务
      log.error("发生错误: {}", ex.getMessage(), ex);
      log.info("回滚事务");
      transactionManager.rollback(transactionStatus);
      log.info("回滚事务完成");
    }
  }

  @Test
  void testInsertHasTransactionSuccess() {
    String sql = "INSERT INTO books (title) VALUES (?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    // 打印事务开启后的状态
    printTransactionStatus("事务开启后", transactionStatus);
    try {
      jdbcTemplate.update(sql, "测试图书1");
      // 打印执行第一条SQL后的状态
      printTransactionStatus("执行第一条SQL后", transactionStatus);
      jdbcTemplate.update(sql, "测试图书2");
      jdbcTemplate.update(sql, "测试图书3");
      jdbcTemplate.update(sql, "测试图书4");
      // 打印提交前的状态
      printTransactionStatus("提交前", transactionStatus);
      // 提交事务
      log.info("提交事务");
      transactionManager.commit(transactionStatus);
      // 打印提交后的状态
      printTransactionStatus("提交后", transactionStatus);
      log.info("提交事务完成");
    } catch (Exception ex) {
      // 打印异常时的状态
      printTransactionStatus("异常时", transactionStatus);
      // 回滚事务
      log.error("发生错误: {}", ex.getMessage(), ex);
      log.info("回滚事务");
      transactionManager.rollback(transactionStatus);
      // 打印回滚后的状态
      printTransactionStatus("回滚后", transactionStatus);
      log.info("回滚事务完成");
    }
  }

  private void printTransactionStatus(String phase, TransactionStatus status) {
    log.info("=== {} 事务状态 ===", phase);
    log.info("事务类名: {}", status.getClass().getName());
    log.info("是否新事务: {}", status.isNewTransaction());
    log.info("是否有保存点: {}", status.hasSavepoint());
    log.info("是否只读: {}", status.isRollbackOnly());
    log.info("是否已完成: {}", status.isCompleted());
    log.info("========================");
  }

  @Test
  void testInsertTransactionStatusRollbackOnly() {
    String sql = "INSERT INTO books (title) VALUES (?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    transactionStatus.setRollbackOnly(); // 设置为只回滚，即使没有异常也会回滚
    log.info("开启事务完成");
    try {
      jdbcTemplate.update(sql, "测试图书1");
      jdbcTemplate.update(sql, "测试图书2");
      jdbcTemplate.update(sql, "测试图书3");
      jdbcTemplate.update(sql, "测试图书4");
      // 提交事务
      log.info("提交事务");
      transactionManager.commit(transactionStatus);
      log.info("提交事务完成");
    } catch (Exception ex) {
      // 回滚事务
      log.error("发生错误: {}", ex.getMessage(), ex);
      log.info("回滚事务");
      transactionManager.rollback(transactionStatus);
      log.info("回滚事务完成");
    }
  }

  @Test
  void testInsertTransactionStatusSavepoint() {
    String sql = "INSERT INTO books (title) VALUES (?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    log.info("开启事务完成");
    Object savepointA = null;
    Object savepointB = null;
    try {
      jdbcTemplate.update(sql, "测试图书1");
      savepointA = transactionStatus.createSavepoint(); // 创建保存点A，后续可以回滚到这里
      jdbcTemplate.update(sql, "测试图书2");
      savepointB = transactionStatus.createSavepoint(); // 创建保存点B，后续可以回滚到这里
      jdbcTemplate.update(sql, "测试图书3");
      jdbcTemplate.update(sql, (String) null); // 错误数据
      jdbcTemplate.update(sql, "测试图书4");
      // 提交事务
      log.info("提交事务");
      transactionManager.commit(transactionStatus);
      log.info("提交事务完成");
    } catch (Exception ex) {
      // 回滚事务
      log.error("发生错误: {}", ex.getMessage(), ex);
      log.info("回滚到指定保存点");
      transactionStatus.rollbackToSavepoint(savepointA); // 回滚到保存点A
      log.info("回滚到指定保存点完成");
      log.info("提交事务");
      transactionManager.commit(transactionStatus); // 提交剩余的事务
      log.info("提交事务完成");
    }
  }

}
