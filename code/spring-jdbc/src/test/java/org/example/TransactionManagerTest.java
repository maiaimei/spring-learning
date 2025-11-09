package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Slf4j
@ContextConfiguration(classes = {Application.class})
@ExtendWith(SpringExtension.class)
public class TransactionManagerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerTest.class);

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
  void testInsert_NonTransaction_Failed() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, "测试图书1", "测试作者", "978-0-000-00000-1", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
    jdbcTemplate.update(sql, "测试图书2", "测试作者", "978-0-000-00000-2", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
    jdbcTemplate.update(sql, null, "测试作者", "978-0-000-00000-3", new BigDecimal("99.99"), LocalDate.now(), "测试", 10); // 错误数据
    jdbcTemplate.update(sql, "测试图书4", "测试作者", "978-0-000-00000-4", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
  }

  @Test
  void testInsert_HasTransaction_Failed() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    log.info("开启事务完成");
    try {
      jdbcTemplate.update(sql, "测试图书1", "测试作者", "978-0-000-00000-1", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      jdbcTemplate.update(sql, "测试图书2", "测试作者", "978-0-000-00000-2", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      jdbcTemplate.update(sql, null, "测试作者", "978-0-000-00000-3", new BigDecimal("99.99"), LocalDate.now(), "测试", 10); // 错误数据
      jdbcTemplate.update(sql, "测试图书4", "测试作者", "978-0-000-00000-4", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
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
  void testInsert_HasTransaction_Success() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);

    // 打印事务开启后的状态
    printTransactionStatus("事务开启后", transactionStatus);

    try {
      jdbcTemplate.update(sql, "测试图书1", "测试作者", "978-0-000-00000-1", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);

      // 打印执行第一条SQL后的状态
      printTransactionStatus("执行第一条SQL后", transactionStatus);

      jdbcTemplate.update(sql, "测试图书2", "测试作者", "978-0-000-00000-2", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      jdbcTemplate.update(sql, "测试图书3", "测试作者", "978-0-000-00000-3", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      jdbcTemplate.update(sql, "测试图书4", "测试作者", "978-0-000-00000-4", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);

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
  void testInsert_TransactionStatus_RollbackOnly() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    transactionStatus.setRollbackOnly();
    log.info("开启事务完成");
    try {
      jdbcTemplate.update(sql, "测试图书1", "测试作者", "978-0-000-00000-1", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      jdbcTemplate.update(sql, "测试图书2", "测试作者", "978-0-000-00000-2", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      jdbcTemplate.update(sql, "测试图书3", "测试作者", "978-0-000-00000-3", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      jdbcTemplate.update(sql, "测试图书4", "测试作者", "978-0-000-00000-4", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
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
  void testInsert_TransactionStatus_Savepoint() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    log.info("开启事务完成");
    Object savepointA = null;
    Object savepointB = null;
    try {
      jdbcTemplate.update(sql, "测试图书1", "测试作者", "978-0-000-00000-1", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      savepointA = transactionStatus.createSavepoint();
      jdbcTemplate.update(sql, "测试图书2", "测试作者", "978-0-000-00000-2", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      savepointB = transactionStatus.createSavepoint();
      jdbcTemplate.update(sql, null, "测试作者", "978-0-000-00000-3", new BigDecimal("99.99"), LocalDate.now(), "测试", 10); // 错误数据
      jdbcTemplate.update(sql, "测试图书4", "测试作者", "978-0-000-00000-4", new BigDecimal("99.99"), LocalDate.now(), "测试",
          10);
      // 提交事务
      log.info("提交事务");
      transactionManager.commit(transactionStatus);
      log.info("提交事务完成");
    } catch (Exception ex) {
      // 回滚事务
      log.error("发生错误: {}", ex.getMessage(), ex);
      log.info("回滚到指定保存点");
      transactionStatus.rollbackToSavepoint(savepointA);
      log.info("回滚到指定保存点完成");
      log.info("提交事务");
      transactionManager.commit(transactionStatus);
      log.info("提交事务完成");
    }
  }

}
