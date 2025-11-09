package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class TransactionPropagationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPropagationTest.class);

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
   * 测试REQUIRED传播机制 - 默认行为
   * 如果当前没有事务，就新建一个事务；如果已经存在事务，则加入该事务
   */
  @Test
  void testRequired() {
    LOGGER.info("=== 测试REQUIRED传播机制 ===");

    // 外层事务
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      LOGGER.info("外层事务开始，是否新事务: {}", outerStatus.isNewTransaction());

      // 插入第一条记录
      insertBook("测试图书1", "978-0-000-00001-0", new BigDecimal("100.00"));

      // 内层事务（使用REQUIRED）
      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        LOGGER.info("内层事务开始，是否新事务: {} (应该是false，加入外层事务)", innerStatus.isNewTransaction());

        // 插入第二条记录
        insertBook("测试图书2", "978-0-000-00002-0", new BigDecimal("120.00"));

        transactionManager.commit(innerStatus);
        LOGGER.info("内层事务提交");
      } catch (Exception e) {
        LOGGER.error("内层事务异常", e);
        transactionManager.rollback(innerStatus);
      }

      transactionManager.commit(outerStatus);
      LOGGER.info("外层事务提交");

      // 验证结果
      int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE title LIKE '测试图书%'", Integer.class);
      LOGGER.info("最终插入记录数: {} (应该是2)", count);

    } catch (Exception e) {
      LOGGER.error("外层事务异常", e);
      transactionManager.rollback(outerStatus);
    }
  }

  /**
   * 测试REQUIRES_NEW传播机制
   * 总是新建一个新的事务，如果存在父级事务则会自动将其挂起
   */
  @Test
  void testRequiresNew() {
    LOGGER.info("=== 测试REQUIRES_NEW传播机制 ===");

    // 外层事务
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      LOGGER.info("外层事务开始，是否新事务: {}", outerStatus.isNewTransaction());

      // 插入第一条记录
      insertBook("测试图书3", "978-0-000-00003-0", new BigDecimal("100.00"));

      // 内层事务（使用REQUIRES_NEW）
      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        LOGGER.info("内层事务开始，是否新事务: {} (应该是true，独立的新事务)", innerStatus.isNewTransaction());

        // 插入第二条记录
        insertBook("测试图书4", "978-0-000-00004-0", new BigDecimal("120.00"));

        transactionManager.commit(innerStatus);
        LOGGER.info("内层事务提交（独立提交，不受外层事务影响）");
      } catch (Exception e) {
        LOGGER.error("内层事务异常", e);
        transactionManager.rollback(innerStatus);
      }

      // 模拟外层事务异常
      LOGGER.info("模拟外层事务异常，回滚外层事务");
      throw new RuntimeException("外层事务异常");

    } catch (Exception e) {
      LOGGER.error("外层事务异常: {}", e.getMessage());
      transactionManager.rollback(outerStatus);

      // 验证结果：内层事务应该已提交，外层事务回滚
      int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE title IN ('测试图书3', '测试图书4')",
          Integer.class);
      LOGGER.info("最终插入记录数: {} (应该是1，只有内层事务的记录)", count);
    }
  }

  /**
   * 测试SUPPORTS传播机制
   * 如果当前存在事务，则加入该事务；如果当前不存在事务，则以非事务方式运行
   */
  @Test
  void testSupports() {
    LOGGER.info("=== 测试SUPPORTS传播机制 ===");

    // 场景1：没有外层事务
    LOGGER.info("场景1：没有外层事务的情况");
    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
    TransactionStatus status1 = transactionManager.getTransaction(def1);

    try {
      LOGGER.info("SUPPORTS事务状态，是否新事务: {} (应该是false，非事务方式运行)", status1.isNewTransaction());
      insertBook("测试图书5", "978-0-000-00005-0", new BigDecimal("100.00"));
      transactionManager.commit(status1);
    } catch (Exception e) {
      transactionManager.rollback(status1);
    }

    // 场景2：有外层事务
    LOGGER.info("场景2：有外层事务的情况");
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      LOGGER.info("外层事务开始");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        LOGGER.info("SUPPORTS内层事务，是否新事务: {} (应该是false，加入外层事务)", innerStatus.isNewTransaction());
        insertBook("测试图书6", "978-0-000-00006-0", new BigDecimal("120.00"));
        transactionManager.commit(innerStatus);
      } catch (Exception e) {
        transactionManager.rollback(innerStatus);
      }

      transactionManager.commit(outerStatus);
    } catch (Exception e) {
      transactionManager.rollback(outerStatus);
    }
  }

  /**
   * 测试NOT_SUPPORTED传播机制
   * 以非事务方式运行，如果当前存在事务，则先挂起父级事务
   */
  @Test
  void testNotSupported() {
    LOGGER.info("=== 测试NOT_SUPPORTED传播机制 ===");

    // 外层事务
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      LOGGER.info("外层事务开始");
      insertBook("测试图书7", "978-0-000-00007-0", new BigDecimal("100.00"));

      // 内层使用NOT_SUPPORTED
      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        LOGGER.info("NOT_SUPPORTED内层事务，是否新事务: {} (应该是false，非事务方式)", innerStatus.isNewTransaction());
        insertBook("测试图书8", "978-0-000-00008-0", new BigDecimal("120.00"));
        transactionManager.commit(innerStatus);
        LOGGER.info("内层非事务操作完成（立即提交到数据库）");
      } catch (Exception e) {
        transactionManager.rollback(innerStatus);
      }

      // 外层事务回滚
      LOGGER.info("外层事务回滚");
      throw new RuntimeException("外层事务异常");

    } catch (Exception e) {
      LOGGER.error("外层事务异常: {}", e.getMessage());
      transactionManager.rollback(outerStatus);

      // 验证结果：NOT_SUPPORTED的操作不受外层事务回滚影响
      int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE title IN ('测试图书7', '测试图书8')",
          Integer.class);
      LOGGER.info("最终插入记录数: {} (应该是1，只有NOT_SUPPORTED的记录)", count);
    }
  }

  /**
   * 测试MANDATORY传播机制
   * 如果当前存在事务，则运行在该事务中；如果当前无事务则抛出异常
   */
  @Test
  void testMandatory() {
    LOGGER.info("=== 测试MANDATORY传播机制 ===");

    // 场景1：没有外层事务（应该抛异常）
    LOGGER.info("场景1：没有外层事务，应该抛异常");
    try {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
      TransactionStatus status = transactionManager.getTransaction(def);
      LOGGER.error("不应该执行到这里");
      transactionManager.rollback(status);
    } catch (Exception e) {
      LOGGER.info("✓ 预期异常: {}", e.getMessage());
    }

    // 场景2：有外层事务（应该正常运行）
    LOGGER.info("场景2：有外层事务，应该正常运行");
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      LOGGER.info("外层事务开始");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        LOGGER.info("MANDATORY内层事务正常运行，是否新事务: {}", innerStatus.isNewTransaction());
        insertBook("测试图书9", "978-0-000-00009-0", new BigDecimal("100.00"));
        transactionManager.commit(innerStatus);
      } catch (Exception e) {
        transactionManager.rollback(innerStatus);
      }

      transactionManager.commit(outerStatus);
      LOGGER.info("✓ MANDATORY在有事务环境下正常运行");
    } catch (Exception e) {
      transactionManager.rollback(outerStatus);
    }
  }

  /**
   * 测试NEVER传播机制
   * 以非事务方式执行，如果当前存在事务，则抛出异常
   */
  @Test
  void testNever() {
    LOGGER.info("=== 测试NEVER传播机制 ===");

    // 场景1：没有外层事务（应该正常运行）
    LOGGER.info("场景1：没有外层事务，应该正常运行");
    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER);
    TransactionStatus status1 = transactionManager.getTransaction(def1);

    try {
      LOGGER.info("NEVER事务状态，是否新事务: {} (应该是false，非事务方式)", status1.isNewTransaction());
      insertBook("测试图书10", "978-0-000-00010-0", new BigDecimal("100.00"));
      transactionManager.commit(status1);
      LOGGER.info("✓ NEVER在无事务环境下正常运行");
    } catch (Exception e) {
      transactionManager.rollback(status1);
    }

    // 场景2：有外层事务（应该抛异常）
    LOGGER.info("场景2：有外层事务，应该抛异常");
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      LOGGER.info("外层事务开始");

      try {
        DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
        innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER);
        TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);
        LOGGER.error("不应该执行到这里");
        transactionManager.rollback(innerStatus);
      } catch (Exception e) {
        LOGGER.info("✓ 预期异常: {}", e.getMessage());
      }

      transactionManager.commit(outerStatus);
    } catch (Exception e) {
      transactionManager.rollback(outerStatus);
    }
  }

  /**
   * 测试NESTED传播机制
   * 如果当前存在事务，则在嵌套事务内执行；如果当前没有事务，等同于REQUIRED
   */
  @Test
  void testNested() {
    LOGGER.info("=== 测试NESTED传播机制 ===");

    // 外层事务
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      LOGGER.info("外层事务开始");
      insertBook("测试图书11", "978-0-000-00011-0", new BigDecimal("100.00"));

      // 内层嵌套事务
      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        LOGGER.info("NESTED内层事务，是否新事务: {}, 是否有保存点: {}",
            innerStatus.isNewTransaction(), innerStatus.hasSavepoint());

        insertBook("测试图书12", "978-0-000-00012-0", new BigDecimal("120.00"));

        // 模拟内层事务异常
        LOGGER.info("模拟内层事务异常");
        throw new RuntimeException("内层事务异常");

      } catch (Exception e) {
        LOGGER.error("内层事务异常: {}", e.getMessage());
        transactionManager.rollback(innerStatus);
        LOGGER.info("内层事务回滚（只回滚到保存点）");
      }

      // 外层事务继续
      insertBook("测试图书13", "978-0-000-00013-0", new BigDecimal("130.00"));

      transactionManager.commit(outerStatus);
      LOGGER.info("外层事务提交");

      // 验证结果：内层事务回滚，外层事务正常提交
      int count = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM books WHERE title IN ('测试图书11', '测试图书12', '测试图书13')", Integer.class);
      LOGGER.info("最终插入记录数: {} (应该是2，内层事务回滚，外层事务正常)", count);

    } catch (Exception e) {
      LOGGER.error("外层事务异常", e);
      transactionManager.rollback(outerStatus);
    }
  }

  /**
   * 传播机制总结测试
   */
  @Test
  void testPropagationSummary() {
    LOGGER.info("=== 事务传播机制总结 ===");
    LOGGER.info("REQUIRED: 默认行为，加入现有事务或创建新事务");
    LOGGER.info("REQUIRES_NEW: 总是创建新事务，挂起现有事务");
    LOGGER.info("SUPPORTS: 支持现有事务，无事务时非事务运行");
    LOGGER.info("NOT_SUPPORTED: 非事务运行，挂起现有事务");
    LOGGER.info("MANDATORY: 必须在事务中运行，否则抛异常");
    LOGGER.info("NEVER: 必须非事务运行，有事务时抛异常");
    LOGGER.info("NESTED: 嵌套事务，使用保存点机制");
  }

  /**
   * 辅助方法：插入图书记录
   */
  private void insertBook(String title, String isbn, BigDecimal price) {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, title, "测试作者", isbn, price, LocalDate.now(), "测试", 10);
    LOGGER.info("插入图书: {}, 价格: {}", title, price);
  }
}
