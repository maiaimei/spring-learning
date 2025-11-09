package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@ContextConfiguration(classes = {Application.class})
@ExtendWith(SpringExtension.class)
public class TransactionIsolationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionIsolationTest.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @BeforeEach
  void setUp() {
    // 清理测试数据
    jdbcTemplate.execute("DELETE FROM books WHERE title LIKE '测试%'");
  }

  /**
   * 测试READ_UNCOMMITTED隔离级别 - 可能出现脏读
   * 事务A可以读取到事务B未提交的数据
   */
  @Test
  void testReadUncommitted_DirtyRead() throws Exception {
    LOGGER.info("=== 测试READ_UNCOMMITTED隔离级别 - 脏读 ===");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    // 插入初始数据
    String insertSql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(insertSql, "测试图书", "测试作者", "978-0-000-00001-0",
        new BigDecimal("100.00"), LocalDate.now(), "测试", 10);

    CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
      // 事务A：使用READ_UNCOMMITTED隔离级别读取数据
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(100); // 等待事务B开始

        // 第一次读取
        BigDecimal price1 = jdbcTemplate.queryForObject(
            "SELECT price FROM books WHERE title = '测试图书'", BigDecimal.class);
        LOGGER.info("事务A第一次读取价格: {}", price1);

        Thread.sleep(200); // 等待事务B修改数据

        // 第二次读取（可能读到未提交的数据）
        BigDecimal price2 = jdbcTemplate.queryForObject(
            "SELECT price FROM books WHERE title = '测试图书'", BigDecimal.class);
        LOGGER.info("事务A第二次读取价格: {} (可能是脏读)", price2);

        transactionManager.commit(status);
      } catch (Exception e) {
        LOGGER.error("事务A异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
      // 事务B：修改数据但不提交（模拟长时间运行的事务）
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(150); // 让事务A先读取一次

        // 修改价格但不立即提交
        jdbcTemplate.update("UPDATE books SET price = ? WHERE title = ?",
            new BigDecimal("200.00"), "测试图书");
        LOGGER.info("事务B修改价格为200.00（未提交）");

        Thread.sleep(300); // 保持事务打开一段时间

        // 回滚事务（模拟最终不提交的情况）
        transactionManager.rollback(status);
        LOGGER.info("事务B回滚");
      } catch (Exception e) {
        LOGGER.error("事务B异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture.allOf(transaction1, transaction2).get(5, TimeUnit.SECONDS);
    executor.shutdown();

    // 验证最终数据
    BigDecimal finalPrice = jdbcTemplate.queryForObject(
        "SELECT price FROM books WHERE title = '测试图书'", BigDecimal.class);
    LOGGER.info("最终价格: {} (应该还是100.00，因为事务B回滚了)", finalPrice);
  }

  /**
   * 测试READ_COMMITTED隔离级别 - 避免脏读，但可能出现不可重复读
   * 事务A只能读取到事务B已提交的数据
   */
  @Test
  void testReadCommitted_NonRepeatableRead() throws Exception {
    LOGGER.info("=== 测试READ_COMMITTED隔离级别 - 不可重复读 ===");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    // 插入初始数据
    String insertSql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(insertSql, "测试图书2", "测试作者", "978-0-000-00002-0",
        new BigDecimal("100.00"), LocalDate.now(), "测试", 10);

    CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
      // 事务A：使用READ_COMMITTED隔离级别
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        // 第一次读取
        BigDecimal price1 = jdbcTemplate.queryForObject(
            "SELECT price FROM books WHERE title = '测试图书2'", BigDecimal.class);
        LOGGER.info("事务A第一次读取价格: {}", price1);

        Thread.sleep(300); // 等待事务B提交

        // 第二次读取（会读到已提交的新数据）
        BigDecimal price2 = jdbcTemplate.queryForObject(
            "SELECT price FROM books WHERE title = '测试图书2'", BigDecimal.class);
        LOGGER.info("事务A第二次读取价格: {} (不可重复读)", price2);

        transactionManager.commit(status);
      } catch (Exception e) {
        LOGGER.error("事务A异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
      // 事务B：修改数据并提交
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(150); // 让事务A先读取一次

        jdbcTemplate.update("UPDATE books SET price = ? WHERE title = ?",
            new BigDecimal("150.00"), "测试图书2");
        LOGGER.info("事务B修改价格为150.00并提交");

        transactionManager.commit(status);
      } catch (Exception e) {
        LOGGER.error("事务B异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture.allOf(transaction1, transaction2).get(5, TimeUnit.SECONDS);
    executor.shutdown();
  }

  /**
   * 测试REPEATABLE_READ隔离级别 - 避免脏读和不可重复读，但可能出现幻读
   * 事务A在整个事务期间读取同一数据的结果保持一致
   */
  @Test
  void testRepeatableRead_PhantomRead() throws Exception {
    LOGGER.info("=== 测试REPEATABLE_READ隔离级别 - 幻读 ===");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
      // 事务A：使用REPEATABLE_READ隔离级别
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        // 第一次查询记录数
        Integer count1 = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM books WHERE category = '测试'", Integer.class);
        LOGGER.info("事务A第一次查询测试分类图书数量: {}", count1);

        Thread.sleep(300); // 等待事务B插入数据

        // 第二次查询记录数（可能出现幻读）
        Integer count2 = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM books WHERE category = '测试'", Integer.class);
        LOGGER.info("事务A第二次查询测试分类图书数量: {} (可能出现幻读)", count2);

        // 查询具体记录
        List<Map<String, Object>> books = jdbcTemplate.queryForList(
            "SELECT title FROM books WHERE category = '测试'");
        LOGGER.info("事务A查询到的图书: {}", books);

        transactionManager.commit(status);
      } catch (Exception e) {
        LOGGER.error("事务A异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
      // 事务B：插入新数据
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(150); // 让事务A先查询一次

        String insertSql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, "
            + "?, ?)";
        jdbcTemplate.update(insertSql, "测试图书3", "测试作者", "978-0-000-00003-0",
            new BigDecimal("80.00"), LocalDate.now(), "测试", 5);
        LOGGER.info("事务B插入新的测试图书并提交");

        transactionManager.commit(status);
      } catch (Exception e) {
        LOGGER.error("事务B异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture.allOf(transaction1, transaction2).get(5, TimeUnit.SECONDS);
    executor.shutdown();
  }

  /**
   * 测试SERIALIZABLE隔离级别 - 最高隔离级别，避免所有并发问题
   * 事务完全串行化执行
   */
  @Test
  void testSerializable_NoIssues() throws Exception {
    LOGGER.info("=== 测试SERIALIZABLE隔离级别 - 完全隔离 ===");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    // 插入初始数据
    String insertSql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(insertSql, "测试图书4", "测试作者", "978-0-000-00004-0",
        new BigDecimal("120.00"), LocalDate.now(), "测试", 8);

    CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
      // 事务A：使用SERIALIZABLE隔离级别
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        LOGGER.info("事务A开始（SERIALIZABLE）");

        // 读取数据
        BigDecimal price1 = jdbcTemplate.queryForObject(
            "SELECT price FROM books WHERE title = '测试图书4'", BigDecimal.class);
        LOGGER.info("事务A读取价格: {}", price1);

        Thread.sleep(200);

        // 再次读取（应该保持一致）
        BigDecimal price2 = jdbcTemplate.queryForObject(
            "SELECT price FROM books WHERE title = '测试图书4'", BigDecimal.class);
        LOGGER.info("事务A再次读取价格: {} (应该一致)", price2);

        // 查询记录数
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM books WHERE category = '测试'", Integer.class);
        LOGGER.info("事务A查询测试分类图书数量: {}", count);

        transactionManager.commit(status);
        LOGGER.info("事务A提交完成");
      } catch (Exception e) {
        LOGGER.error("事务A异常", e);
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
      // 事务B：尝试修改数据（可能被阻塞）
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        Thread.sleep(100); // 让事务A先开始

        LOGGER.info("事务B尝试修改数据（可能被阻塞）");
        jdbcTemplate.update("UPDATE books SET price = ? WHERE title = ?",
            new BigDecimal("180.00"), "测试图书4");
        LOGGER.info("事务B修改成功");

        transactionManager.commit(status);
        LOGGER.info("事务B提交完成");
      } catch (Exception e) {
        LOGGER.error("事务B异常（可能因为锁等待超时）: {}", e.getMessage());
        transactionManager.rollback(status);
      }
    }, executor);

    CompletableFuture.allOf(transaction1, transaction2).get(10, TimeUnit.SECONDS);
    executor.shutdown();
  }

  /**
   * 演示不同隔离级别的性能对比
   */
  @Test
  void testIsolationLevelPerformance() {
    LOGGER.info("=== 隔离级别性能对比 ===");

    // 测试各个隔离级别的基本操作性能
    int[] isolationLevels = {
        TransactionDefinition.ISOLATION_READ_UNCOMMITTED,
        TransactionDefinition.ISOLATION_READ_COMMITTED,
        TransactionDefinition.ISOLATION_REPEATABLE_READ,
        TransactionDefinition.ISOLATION_SERIALIZABLE
    };

    String[] levelNames = {
        "READ_UNCOMMITTED",
        "READ_COMMITTED",
        "REPEATABLE_READ",
        "SERIALIZABLE"
    };

    for (int i = 0; i < isolationLevels.length; i++) {
      long startTime = System.currentTimeMillis();

      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(isolationLevels[i]);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        // 执行简单的查询操作
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM books", Integer.class);

        transactionManager.commit(status);

        long endTime = System.currentTimeMillis();
        LOGGER.info("{} 隔离级别执行时间: {}ms, 查询结果: {}",
            levelNames[i], endTime - startTime, count);

      } catch (Exception e) {
        LOGGER.error("{} 隔离级别执行异常", levelNames[i], e);
        transactionManager.rollback(status);
      }
    }
  }
}
