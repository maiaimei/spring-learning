package org.example;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Slf4j
@ContextConfiguration(classes = {TestApplication.class})
@ExtendWith(SpringExtension.class)
public class TransactionReadOnlyTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @BeforeEach
  void setUp() {
    // 清理测试数据
    jdbcTemplate.execute("DELETE FROM books WHERE title LIKE '测试%'");
    // 插入测试数据
    jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书1");
    jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书2");
    jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书3");
  }

  @Test
  void testReadOnlyTransactionBasicUsage() {
    log.info("=== 测试只读事务的基本用法 ===");

    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setReadOnly(true);
    TransactionStatus status = transactionManager.getTransaction(def);

    try {
      log.info("只读事务开始，是否只读: {}", status.isReadOnly());

      List<Map<String, Object>> books = jdbcTemplate.queryForList(
          "SELECT id, title FROM books WHERE title LIKE '测试%' ORDER BY title");
      log.info("查询到 {} 本测试图书", books.size());

      for (Map<String, Object> book : books) {
        log.info("图书: ID={}, 标题={}", book.get("id"), book.get("title"));
      }

      Integer totalCount = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM books WHERE title LIKE '测试%'", Integer.class);
      log.info("测试图书总数: {}", totalCount);

      transactionManager.commit(status);
      log.info("✓ 只读事务中的查询操作执行成功");

    } catch (Exception e) {
      log.error("只读事务异常", e);
      transactionManager.rollback(status);
    }
  }

  @Test
  void testReadOnlyTransactionWriteOperation() {
    log.info("=== 测试只读事务中的写操作限制 ===");

    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setReadOnly(true);
    TransactionStatus status = transactionManager.getTransaction(def);

    try {
      log.info("只读事务开始，尝试执行写操作");

      try {
        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试只读插入");
        log.error("不应该执行到这里 - 插入操作应该失败");
      } catch (Exception e) {
        log.info("✓ 预期异常 - 插入操作被拒绝: {}", e.getClass().getSimpleName());
      }

      try {
        jdbcTemplate.update("UPDATE books SET title = ? WHERE title LIKE '测试%'", "更新后的标题");
        log.error("不应该执行到这里 - 更新操作应该失败");
      } catch (Exception e) {
        log.info("✓ 预期异常 - 更新操作被拒绝: {}", e.getClass().getSimpleName());
      }

      try {
        jdbcTemplate.update("DELETE FROM books WHERE title LIKE '测试%'");
        log.error("不应该执行到这里 - 删除操作应该失败");
      } catch (Exception e) {
        log.info("✓ 预期异常 - 删除操作被拒绝: {}", e.getClass().getSimpleName());
      }

      transactionManager.commit(status);

    } catch (Exception e) {
      log.error("只读事务异常", e);
      transactionManager.rollback(status);
    }
  }

  @Test
  void testReadOnlyTransactionPerformance() {
    log.info("=== 测试只读事务的性能优化 ===");

    long normalTransactionTime = measureQueryTime(false);
    log.info("普通事务查询耗时: {} ms", normalTransactionTime);

    long readOnlyTransactionTime = measureQueryTime(true);
    log.info("只读事务查询耗时: {} ms", readOnlyTransactionTime);

    if (readOnlyTransactionTime < normalTransactionTime) {
      log.info("✓ 只读事务性能更优，提升: {} ms", normalTransactionTime - readOnlyTransactionTime);
    } else {
      log.info("性能差异不明显或只读事务稍慢（可能由于测试数据量小）");
    }
  }

  @Test
  void testReadOnlyTransactionNestedWithNormalTransaction() {
    log.info("=== 测试只读事务与普通事务的嵌套 ===");

    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setReadOnly(true);
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      log.info("外层只读事务开始");

      Integer count1 = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM books WHERE title LIKE '测试%'", Integer.class);
      log.info("外层事务查询到 {} 本图书", count1);

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setReadOnly(false);
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        log.info("内层普通事务开始，是否只读: {}", innerStatus.isReadOnly());

        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试嵌套事务");
        log.info("内层事务插入操作成功");

        transactionManager.commit(innerStatus);
        log.info("内层事务提交");

      } catch (Exception e) {
        log.error("内层事务异常", e);
        transactionManager.rollback(innerStatus);
      }

      Integer count2 = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM books WHERE title LIKE '测试%'", Integer.class);
      log.info("外层事务再次查询到 {} 本图书", count2);

      transactionManager.commit(outerStatus);
      log.info("外层只读事务提交");

    } catch (Exception e) {
      log.error("外层只读事务异常", e);
      transactionManager.rollback(outerStatus);
    }
  }

  @Test
  void testReadOnlyTransactionIsolationLevel() {
    log.info("=== 测试只读事务的隔离级别 ===");

    int[] isolationLevels = {
        TransactionDefinition.ISOLATION_READ_COMMITTED,
        TransactionDefinition.ISOLATION_REPEATABLE_READ
    };

    String[] levelNames = {
        "READ_COMMITTED",
        "REPEATABLE_READ"
    };

    for (int i = 0; i < isolationLevels.length; i++) {
      log.info("测试 {} 隔离级别的只读事务", levelNames[i]);

      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setReadOnly(true);
      def.setIsolationLevel(isolationLevels[i]);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        List<Map<String, Object>> books = jdbcTemplate.queryForList(
            "SELECT id, title FROM books WHERE title LIKE '测试%' ORDER BY id DESC LIMIT 3");

        log.info("{} 隔离级别查询结果:", levelNames[i]);
        for (Map<String, Object> book : books) {
          log.info("  - ID={}: {}", book.get("id"), book.get("title"));
        }

        transactionManager.commit(status);

      } catch (Exception e) {
        log.error("{} 隔离级别只读事务异常", levelNames[i], e);
        transactionManager.rollback(status);
      }
    }
  }

  @Test
  void testReadOnlyTransactionTimeout() {
    log.info("=== 测试只读事务的超时设置 ===");

    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setReadOnly(true);
    def.setTimeout(5);
    TransactionStatus status = transactionManager.getTransaction(def);

    try {
      log.info("只读事务开始，超时时间: 5秒");

      for (int i = 0; i < 3; i++) {
        List<Map<String, Object>> books = jdbcTemplate.queryForList(
            "SELECT * FROM books WHERE title LIKE '测试%'");
        log.info("第 {} 次查询，结果数量: {}", i + 1, books.size());

        Thread.sleep(100);
      }

      transactionManager.commit(status);
      log.info("✓ 只读事务在超时时间内正常完成");

    } catch (Exception e) {
      log.error("只读事务异常（可能超时）", e);
      transactionManager.rollback(status);
    }
  }

  private long measureQueryTime(boolean readOnly) {
    long startTime = System.currentTimeMillis();

    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setReadOnly(readOnly);
    TransactionStatus status = transactionManager.getTransaction(def);

    try {
      for (int i = 0; i < 10; i++) {
        jdbcTemplate.queryForList("SELECT * FROM books WHERE title LIKE '测试%'");
      }

      transactionManager.commit(status);
    } catch (Exception e) {
      transactionManager.rollback(status);
    }

    return System.currentTimeMillis() - startTime;
  }

}