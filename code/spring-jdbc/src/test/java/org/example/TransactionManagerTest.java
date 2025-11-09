package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
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

  @Test
  void testInsertExceptionWithoutTransaction() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, "测试图书1", "测试作者", "978-0-000-00000-1", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
    jdbcTemplate.update(sql, "测试图书2", "测试作者", "978-0-000-00000-2", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
    jdbcTemplate.update(sql, null, "测试作者", "978-0-000-00000-3", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
    jdbcTemplate.update(sql, "测试图书4", "测试作者", "978-0-000-00000-4", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
  }

  @Test
  void testInsertExceptionWithTransaction() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    log.info("开启事务完成");
    try {
      jdbcTemplate.update(sql, "测试图书1", "测试作者", "978-0-000-00000-1", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
      jdbcTemplate.update(sql, "测试图书2", "测试作者", "978-0-000-00000-2", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
      jdbcTemplate.update(sql, null, "测试作者", "978-0-000-00000-3", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
      jdbcTemplate.update(sql, "测试图书4", "测试作者", "978-0-000-00000-4", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
      // 提交事务
      log.info("提交事务");
      transactionManager.commit(transactionStatus);
      log.info("提交事务完成");
    } catch (Exception ex) {
      // 回滚事务
      log.error("回滚事务，发生错误: {}", ex.getMessage(), ex);
      transactionManager.rollback(transactionStatus);
      log.info("回滚事务完成");
    }
  }

  @Test
  void testInsertSuccessWithTransaction() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 开启事务
    log.info("开启事务");
    final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    log.info("开启事务完成");
    try {
      jdbcTemplate.update(sql, "测试图书1", "测试作者", "978-0-000-00000-1", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
      jdbcTemplate.update(sql, "测试图书2", "测试作者", "978-0-000-00000-2", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
      jdbcTemplate.update(sql, "测试图书3", "测试作者", "978-0-000-00000-3", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
      jdbcTemplate.update(sql, "测试图书4", "测试作者", "978-0-000-00000-4", new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
      // 提交事务
      log.info("提交事务");
      transactionManager.commit(transactionStatus);
      log.info("提交事务完成");
    } catch (Exception ex) {
      // 回滚事务
      log.error("回滚事务，发生错误: {}", ex.getMessage(), ex);
      transactionManager.rollback(transactionStatus);
      log.info("回滚事务完成");
    }
  }

}
