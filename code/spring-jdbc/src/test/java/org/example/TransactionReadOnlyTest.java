package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
public class TransactionReadOnlyTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionReadOnlyTest.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @BeforeEach
  void setUp() {
    // 清理测试数据
    jdbcTemplate.execute("DELETE FROM books WHERE title LIKE '测试%'");
    // 插入测试数据
    insertTestData();
  }

  /**
   * 测试只读事务的基本用法
   * 只读事务只能执行查询操作，不能执行增删改操作
   */
  @Test
  void testReadOnlyTransaction_BasicUsage() {
    LOGGER.info("=== 测试只读事务的基本用法 ===");

    // 创建只读事务
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setReadOnly(true);
    TransactionStatus status = transactionManager.getTransaction(def);

    try {
      LOGGER.info("只读事务开始，是否只读: {}", status.isReadOnly());

      // 执行查询操作（应该正常）
      List<Map<String, Object>> books = jdbcTemplate.queryForList(
          "SELECT title, author, price FROM books WHERE category = '测试' ORDER BY title");
      LOGGER.info("查询到 {} 本测试图书", books.size());

      for (Map<String, Object> book : books) {
        LOGGER.info("图书: {}, 作者: {}, 价格: {}",
            book.get("title"), book.get("author"), book.get("price"));
      }

      // 统计查询
      Integer totalCount = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM books WHERE category = '测试'", Integer.class);
      BigDecimal avgPrice = jdbcTemplate.queryForObject(
          "SELECT AVG(price) FROM books WHERE category = '测试'", BigDecimal.class);

      LOGGER.info("测试图书总数: {}, 平均价格: {}", totalCount, avgPrice);

      transactionManager.commit(status);
      LOGGER.info("✓ 只读事务中的查询操作执行成功");

    } catch (Exception e) {
      LOGGER.error("只读事务异常", e);
      transactionManager.rollback(status);
    }
  }

  /**
   * 测试只读事务中尝试执行写操作
   * 应该抛出异常或被数据库拒绝
   */
  @Test
  void testReadOnlyTransaction_WriteOperation() {
    LOGGER.info("=== 测试只读事务中的写操作限制 ===");

    // 创建只读事务
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setReadOnly(true);
    TransactionStatus status = transactionManager.getTransaction(def);

    try {
      LOGGER.info("只读事务开始，尝试执行写操作");

      // 尝试插入操作（应该失败）
      try {
        String insertSql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, "
            + "?, ?)";
        jdbcTemplate.update(insertSql, "测试只读插入", "测试作者", "978-0-000-99999-0",
            new BigDecimal("99.99"), LocalDate.now(), "测试", 1);
        LOGGER.error("不应该执行到这里 - 插入操作应该失败");
      } catch (Exception e) {
        LOGGER.info("✓ 预期异常 - 插入操作被拒绝: {}", e.getClass().getSimpleName());
      }

      // 尝试更新操作（应该失败）
      try {
        jdbcTemplate.update("UPDATE books SET price = ? WHERE category = '测试'",
            new BigDecimal("999.99"));
        LOGGER.error("不应该执行到这里 - 更新操作应该失败");
      } catch (Exception e) {
        LOGGER.info("✓ 预期异常 - 更新操作被拒绝: {}", e.getClass().getSimpleName());
      }

      // 尝试删除操作（应该失败）
      try {
        jdbcTemplate.update("DELETE FROM books WHERE category = '测试'");
        LOGGER.error("不应该执行到这里 - 删除操作应该失败");
      } catch (Exception e) {
        LOGGER.info("✓ 预期异常 - 删除操作被拒绝: {}", e.getClass().getSimpleName());
      }

      transactionManager.commit(status);

    } catch (Exception e) {
      LOGGER.error("只读事务异常", e);
      transactionManager.rollback(status);
    }
  }

  /**
   * 测试只读事务的性能优化
   * 对比只读事务和普通事务的执行时间
   */
  @Test
  void testReadOnlyTransaction_Performance() {
    LOGGER.info("=== 测试只读事务的性能优化 ===");

    // 测试普通事务的查询性能
    long normalTransactionTime = measureQueryTime(false);
    LOGGER.info("普通事务查询耗时: {} ms", normalTransactionTime);

    // 测试只读事务的查询性能
    long readOnlyTransactionTime = measureQueryTime(true);
    LOGGER.info("只读事务查询耗时: {} ms", readOnlyTransactionTime);

    // 性能对比
    if (readOnlyTransactionTime < normalTransactionTime) {
      LOGGER.info("✓ 只读事务性能更优，提升: {} ms",
          normalTransactionTime - readOnlyTransactionTime);
    } else {
      LOGGER.info("性能差异不明显或只读事务稍慢（可能由于测试数据量小）");
    }
  }

  /**
   * 测试只读事务与普通事务的嵌套
   * 只读事务中调用普通事务方法
   */
  @Test
  void testReadOnlyTransaction_NestedWithNormalTransaction() {
    LOGGER.info("=== 测试只读事务与普通事务的嵌套 ===");

    // 外层只读事务
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setReadOnly(true);
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      LOGGER.info("外层只读事务开始");

      // 查询操作
      Integer count1 = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM books WHERE category = '测试'", Integer.class);
      LOGGER.info("外层事务查询到 {} 本图书", count1);

      // 内层普通事务（使用REQUIRES_NEW创建独立事务）
      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setReadOnly(false);
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        LOGGER.info("内层普通事务开始，是否只读: {}", innerStatus.isReadOnly());

        // 在内层事务中执行写操作
        String insertSql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, "
            + "?, ?)";
        jdbcTemplate.update(insertSql, "测试嵌套事务", "测试作者", "978-0-000-88888-0",
            new BigDecimal("88.88"), LocalDate.now(), "测试", 1);
        LOGGER.info("内层事务插入操作成功");

        transactionManager.commit(innerStatus);
        LOGGER.info("内层事务提交");

      } catch (Exception e) {
        LOGGER.error("内层事务异常", e);
        transactionManager.rollback(innerStatus);
      }

      // 外层事务继续查询
      Integer count2 = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM books WHERE category = '测试'", Integer.class);
      LOGGER.info("外层事务再次查询到 {} 本图书", count2);

      transactionManager.commit(outerStatus);
      LOGGER.info("外层只读事务提交");

    } catch (Exception e) {
      LOGGER.error("外层只读事务异常", e);
      transactionManager.rollback(outerStatus);
    }
  }

  /**
   * 测试只读事务的隔离级别
   * 只读事务通常使用较低的隔离级别以提高性能
   */
  @Test
  void testReadOnlyTransaction_IsolationLevel() {
    LOGGER.info("=== 测试只读事务的隔离级别 ===");

    // 测试不同隔离级别的只读事务
    int[] isolationLevels = {
        TransactionDefinition.ISOLATION_READ_COMMITTED,
        TransactionDefinition.ISOLATION_REPEATABLE_READ
    };

    String[] levelNames = {
        "READ_COMMITTED",
        "REPEATABLE_READ"
    };

    for (int i = 0; i < isolationLevels.length; i++) {
      LOGGER.info("测试 {} 隔离级别的只读事务", levelNames[i]);

      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setReadOnly(true);
      def.setIsolationLevel(isolationLevels[i]);
      TransactionStatus status = transactionManager.getTransaction(def);

      try {
        // 执行查询操作
        List<Map<String, Object>> books = jdbcTemplate.queryForList(
            "SELECT title, price FROM books WHERE category = '测试' ORDER BY price DESC LIMIT 3");

        LOGGER.info("{} 隔离级别查询结果:", levelNames[i]);
        for (Map<String, Object> book : books) {
          LOGGER.info("  - {}: {}", book.get("title"), book.get("price"));
        }

        transactionManager.commit(status);

      } catch (Exception e) {
        LOGGER.error("{} 隔离级别只读事务异常", levelNames[i], e);
        transactionManager.rollback(status);
      }
    }
  }

  /**
   * 测试只读事务的超时设置
   * 只读事务也可以设置超时时间
   */
  @Test
  void testReadOnlyTransaction_Timeout() {
    LOGGER.info("=== 测试只读事务的超时设置 ===");

    // 创建带超时的只读事务
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setReadOnly(true);
    def.setTimeout(5); // 5秒超时
    TransactionStatus status = transactionManager.getTransaction(def);

    try {
      LOGGER.info("只读事务开始，超时时间: 5秒");

      // 执行一些查询操作
      for (int i = 0; i < 3; i++) {
        List<Map<String, Object>> books = jdbcTemplate.queryForList(
            "SELECT * FROM books WHERE category = '测试'");
        LOGGER.info("第 {} 次查询，结果数量: {}", i + 1, books.size());

        // 模拟一些处理时间
        Thread.sleep(100);
      }

      transactionManager.commit(status);
      LOGGER.info("✓ 只读事务在超时时间内正常完成");

    } catch (Exception e) {
      LOGGER.error("只读事务异常（可能超时）", e);
      transactionManager.rollback(status);
    }
  }

  /**
   * 只读事务使用场景总结
   */
  @Test
  void testReadOnlyTransaction_UseCases() {
    LOGGER.info("=== 只读事务使用场景总结 ===");
    LOGGER.info("1. 报表查询：大量数据的统计分析");
    LOGGER.info("2. 数据导出：批量数据读取和导出");
    LOGGER.info("3. 只读API：提供数据查询服务的接口");
    LOGGER.info("4. 数据校验：验证数据完整性和一致性");
    LOGGER.info("5. 缓存预热：预加载数据到缓存");

    LOGGER.info("\n只读事务的优势:");
    LOGGER.info("- 性能优化：数据库可以进行读优化");
    LOGGER.info("- 安全保障：防止意外的数据修改");
    LOGGER.info("- 资源节省：减少锁的使用");
    LOGGER.info("- 明确意图：代码意图更加清晰");
  }

  /**
   * 辅助方法：插入测试数据
   */
  private void insertTestData() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";

    jdbcTemplate.update(sql, "测试图书A", "作者A", "978-0-000-00001-0",
        new BigDecimal("59.99"), LocalDate.now(), "测试", 20);
    jdbcTemplate.update(sql, "测试图书B", "作者B", "978-0-000-00002-0",
        new BigDecimal("79.99"), LocalDate.now(), "测试", 15);
    jdbcTemplate.update(sql, "测试图书C", "作者C", "978-0-000-00003-0",
        new BigDecimal("99.99"), LocalDate.now(), "测试", 10);
    jdbcTemplate.update(sql, "测试图书D", "作者D", "978-0-000-00004-0",
        new BigDecimal("129.99"), LocalDate.now(), "测试", 8);

    LOGGER.info("插入了4本测试图书");
  }

  /**
   * 辅助方法：测量查询时间
   */
  private long measureQueryTime(boolean readOnly) {
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setReadOnly(readOnly);

    long startTime = System.currentTimeMillis();
    TransactionStatus status = transactionManager.getTransaction(def);

    try {
      // 执行多次查询操作
      for (int i = 0; i < 10; i++) {
        jdbcTemplate.queryForList("SELECT * FROM books WHERE category = '测试'");
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE category = '测试'", Integer.class);
        jdbcTemplate.queryForObject("SELECT AVG(price) FROM books WHERE category = '测试'", BigDecimal.class);
      }

      transactionManager.commit(status);

    } catch (Exception e) {
      transactionManager.rollback(status);
    }

    return System.currentTimeMillis() - startTime;
  }
}
