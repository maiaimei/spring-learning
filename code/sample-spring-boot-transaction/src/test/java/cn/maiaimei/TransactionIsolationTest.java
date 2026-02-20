package cn.maiaimei;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
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
public class TransactionIsolationTest {

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
  void testReadUncommitted() throws Exception {
    log.info("=== 测试READ_UNCOMMITTED隔离级别 - 脏读 ===");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    // 插入初始数据
    jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书");

    CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(100);

        String title1 = jdbcTemplate.queryForObject(
            "SELECT title FROM books WHERE title = '测试图书'", String.class);
        log.info("事务A第一次读取标题: {}", title1);

        Thread.sleep(200);

        String title2 = jdbcTemplate.queryForObject(
            "SELECT title FROM books WHERE title LIKE '测试图书%'", String.class);
        log.info("事务A第二次读取标题: {} (可能是脏读)", title2);

        transactionManager.commit(status);
      } catch (Exception e) {
        log.error("事务A异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(150);

        jdbcTemplate.update("UPDATE books SET title = ? WHERE title = ?",
            "测试图书-修改版", "测试图书");
        log.info("事务B修改标题为'测试图书-修改版'（未提交）");

        Thread.sleep(300);

        transactionManager.rollback(status);
        log.info("事务B回滚");
      } catch (Exception e) {
        log.error("事务B异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture.allOf(transaction1, transaction2).get(5, TimeUnit.SECONDS);
    executor.shutdown();

    String finalTitle = jdbcTemplate.queryForObject(
        "SELECT title FROM books WHERE title LIKE '测试图书%'", String.class);
    log.info("最终标题: {} (应该还是'测试图书'，因为事务B回滚了)", finalTitle);
  }

  @Test
  void testReadCommitted() throws Exception {
    log.info("=== 测试READ_COMMITTED隔离级别 - 不可重复读 ===");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书2");

    CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        String title1 = jdbcTemplate.queryForObject(
            "SELECT title FROM books WHERE title = '测试图书2'", String.class);
        log.info("事务A第一次读取标题: {}", title1);

        Thread.sleep(300);

        String title2 = jdbcTemplate.queryForObject(
            "SELECT title FROM books WHERE title LIKE '测试图书2%'", String.class);
        log.info("事务A第二次读取标题: {} (不可重复读)", title2);

        transactionManager.commit(status);
      } catch (Exception e) {
        log.error("事务A异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(150);

        jdbcTemplate.update("UPDATE books SET title = ? WHERE title = ?",
            "测试图书2-已修改", "测试图书2");
        log.info("事务B修改标题为'测试图书2-已修改'并提交");

        transactionManager.commit(status);
      } catch (Exception e) {
        log.error("事务B异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture.allOf(transaction1, transaction2).get(5, TimeUnit.SECONDS);
    executor.shutdown();
  }

  @Test
  void testRepeatableRead() throws Exception {
    log.info("=== 测试REPEATABLE_READ隔离级别 - H2数据库幻读测试 ===");
    log.info("注意：H2数据库在REPEATABLE_READ级别下可能会出现幻读现象");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        log.info("事务A开始，使用REPEATABLE_READ隔离级别");

        // 第一次查询
        Integer count1 = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM books WHERE title LIKE '测试幻读%'", Integer.class);
        log.info("事务A第一次查询测试幻读图书数量: {}", count1);

        List<Map<String, Object>> books1 = jdbcTemplate.queryForList(
            "SELECT id, title FROM books WHERE title LIKE '测试幻读%' ORDER BY title");
        log.info("事务A第一次查询到的图书: {}", books1);

        // 等待事务B插入数据
        Thread.sleep(1000);

        // 第二次查询
        Integer count2 = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM books WHERE title LIKE '测试幻读%'", Integer.class);
        log.info("事务A第二次查询测试幻读图书数量: {}", count2);

        List<Map<String, Object>> books2 = jdbcTemplate.queryForList(
            "SELECT id, title FROM books WHERE title LIKE '测试幻读%' ORDER BY title");
        log.info("事务A第二次查询到的图书: {}", books2);

        if (count1.equals(count2)) {
          log.info("✓ 没有出现幻读现象，数据一致");
        } else {
          log.info("✗ 出现了幻读现象：第一次查询{}条，第二次查询{}条", count1, count2);
        }

        transactionManager.commit(status);
        log.info("事务A提交完成");
      } catch (Exception e) {
        log.error("事务A异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        // 等待事务A先查询一次
        Thread.sleep(200);

        log.info("事务B开始插入新的测试幻读图书");

        // 插入多条数据增加幻读效果
        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试幻读图书1");
        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试幻读图书2");
        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试幻读图书3");

        log.info("事务B插入了3条数据并提交");

        transactionManager.commit(status);
        log.info("事务B提交完成");
      } catch (Exception e) {
        log.error("事务B异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture.allOf(transaction1, transaction2).get(10, TimeUnit.SECONDS);
    executor.shutdown();
  }

  @Test
  void testSerializable() throws Exception {
    log.info("=== 测试SERIALIZABLE隔离级别 - 最高隔离级别 ===");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试串行化图书");

    CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        log.info("事务A开始，使用SERIALIZABLE隔离级别");

        String title = jdbcTemplate.queryForObject(
            "SELECT title FROM books WHERE title = '测试串行化图书'", String.class);
        log.info("事务A读取到标题: {}", title);

        Thread.sleep(300);

        jdbcTemplate.update("UPDATE books SET title = ? WHERE title = ?",
            "测试串行化图书-A修改", "测试串行化图书");
        log.info("事务A修改标题成功");

        transactionManager.commit(status);
        log.info("事务A提交完成");
      } catch (Exception e) {
        log.error("事务A异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(100);

        log.info("事务B尝试读取数据（可能被阻塞）");
        long startTime = System.currentTimeMillis();

        String title = jdbcTemplate.queryForObject(
            "SELECT title FROM books WHERE title LIKE '测试串行化图书%'", String.class);

        long endTime = System.currentTimeMillis();
        log.info("事务B读取到标题: {}，耗时: {} ms", title, endTime - startTime);

        transactionManager.commit(status);
        log.info("事务B提交完成");
      } catch (Exception e) {
        log.error("事务B异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture.allOf(transaction1, transaction2).get(10, TimeUnit.SECONDS);
    executor.shutdown();
  }

  @Test
  void testIsolationLevelComparison() {
    log.info("=== 事务隔离级别对比总结 ===");
    log.info("1. READ_UNCOMMITTED: 允许脏读、不可重复读、幻读");
    log.info("2. READ_COMMITTED: 避免脏读，允许不可重复读、幻读");
    log.info("3. REPEATABLE_READ: 避免脏读、不可重复读，MySQL通过间隙锁也避免了幻读");
    log.info("4. SERIALIZABLE: 避免所有并发问题，但性能最低");
    log.info("");
    log.info("MySQL默认隔离级别: REPEATABLE_READ");
    log.info("PostgreSQL默认隔离级别: READ_COMMITTED");
    log.info("Oracle默认隔离级别: READ_COMMITTED");
  }

}