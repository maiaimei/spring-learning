package cn.maiaimei;

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
public class TransactionPropagationTest {

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
   * Support a current transaction; create a new one if none exists.
   * 如果当前没有事务，就新建一个事务；如果已经存在事务，则加入该事务。
   */
  @Test
  void testRequired() {
    log.info("=== 测试REQUIRED传播机制 ===");

    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      log.info("外层事务开始，是否新事务: {}", outerStatus.isNewTransaction());

      jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书1");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        log.info("内层事务开始，是否新事务: {} (应该是false，加入外层事务)", innerStatus.isNewTransaction());

        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书2");

        transactionManager.commit(innerStatus);
        log.info("内层事务提交");
      } catch (Exception e) {
        log.error("内层事务异常", e);
        transactionManager.rollback(innerStatus);
      }

      transactionManager.commit(outerStatus);
      log.info("外层事务提交");

      int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE title LIKE '测试图书%'", Integer.class);
      log.info("最终插入记录数: {} (应该是2)", count);

    } catch (Exception e) {
      log.error("外层事务异常", e);
      transactionManager.rollback(outerStatus);
    }
  }

  @Test
  void testRequiresNew() {
    log.info("=== 测试REQUIRES_NEW传播机制 ===");

    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      log.info("外层事务开始，是否新事务: {}", outerStatus.isNewTransaction());

      jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书3");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        log.info("内层事务开始，是否新事务: {} (应该是true，独立的新事务)", innerStatus.isNewTransaction());

        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书4");

        transactionManager.commit(innerStatus);
        log.info("内层事务提交（独立提交，不受外层事务影响）");
      } catch (Exception e) {
        log.error("内层事务异常", e);
        transactionManager.rollback(innerStatus);
      }

      log.info("模拟外层事务异常，回滚外层事务");
      throw new RuntimeException("外层事务异常");

    } catch (Exception e) {
      log.error("外层事务异常: {}", e.getMessage());
      transactionManager.rollback(outerStatus);

      int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE title IN ('测试图书3', '测试图书4')",
          Integer.class);
      log.info("最终插入记录数: {} (应该是1，只有内层事务的记录)", count);
    }
  }

  @Test
  void testSupports() {
    log.info("=== 测试SUPPORTS传播机制 ===");

    log.info("场景1：没有外层事务的情况");
    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
    TransactionStatus status1 = transactionManager.getTransaction(def1);

    try {
      log.info("SUPPORTS事务状态，是否新事务: {} (应该是false，非事务方式运行)", status1.isNewTransaction());
      jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书5");
      transactionManager.commit(status1);
    } catch (Exception e) {
      transactionManager.rollback(status1);
    }

    log.info("场景2：有外层事务的情况");
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      log.info("外层事务开始");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        log.info("SUPPORTS内层事务，是否新事务: {} (应该是false，加入外层事务)", innerStatus.isNewTransaction());
        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书6");
        transactionManager.commit(innerStatus);
      } catch (Exception e) {
        transactionManager.rollback(innerStatus);
      }

      transactionManager.commit(outerStatus);
    } catch (Exception e) {
      transactionManager.rollback(outerStatus);
    }
  }

  @Test
  void testNotSupported() {
    log.info("=== 测试NOT_SUPPORTED传播机制 ===");

    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      log.info("外层事务开始");
      jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书7");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        log.info("NOT_SUPPORTED内层事务，是否新事务: {} (应该是false，非事务方式)", innerStatus.isNewTransaction());
        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书8");
        transactionManager.commit(innerStatus);
        log.info("内层非事务操作完成（立即提交到数据库）");
      } catch (Exception e) {
        transactionManager.rollback(innerStatus);
      }

      log.info("外层事务回滚");
      throw new RuntimeException("外层事务异常");

    } catch (Exception e) {
      log.error("外层事务异常: {}", e.getMessage());
      transactionManager.rollback(outerStatus);

      int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE title IN ('测试图书7', '测试图书8')",
          Integer.class);
      log.info("最终插入记录数: {} (应该是1，只有NOT_SUPPORTED的记录)", count);
    }
  }

  @Test
  void testMandatory() {
    log.info("=== 测试MANDATORY传播机制 ===");

    log.info("场景1：没有外层事务，应该抛异常");
    try {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
      TransactionStatus status = transactionManager.getTransaction(def);
      log.error("不应该执行到这里");
      transactionManager.rollback(status);
    } catch (Exception e) {
      log.info("✓ 预期异常: {}", e.getMessage());
    }

    log.info("场景2：有外层事务，应该正常运行");
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      log.info("外层事务开始");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        log.info("MANDATORY内层事务，是否新事务: {} (应该是false，加入外层事务)", innerStatus.isNewTransaction());
        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书9");
        transactionManager.commit(innerStatus);
        log.info("内层MANDATORY事务成功");
      } catch (Exception e) {
        transactionManager.rollback(innerStatus);
      }

      transactionManager.commit(outerStatus);
    } catch (Exception e) {
      transactionManager.rollback(outerStatus);
    }
  }

  @Test
  void testNever() {
    log.info("=== 测试NEVER传播机制 ===");

    log.info("场景1：没有外层事务，应该正常运行");
    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER);
    TransactionStatus status1 = transactionManager.getTransaction(def1);

    try {
      log.info("NEVER事务状态，是否新事务: {} (应该是false，非事务方式)", status1.isNewTransaction());
      jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书10");
      transactionManager.commit(status1);
      log.info("NEVER在无事务环境下正常运行");
    } catch (Exception e) {
      transactionManager.rollback(status1);
    }

    log.info("场景2：有外层事务，应该抛异常");
    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      log.info("外层事务开始");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      log.error("不应该执行到这里");
      transactionManager.rollback(innerStatus);
      transactionManager.rollback(outerStatus);

    } catch (Exception e) {
      log.info("✓ 预期异常: {}", e.getMessage());
      transactionManager.rollback(outerStatus);
    }
  }

  @Test
  void testNested() {
    log.info("=== 测试NESTED传播机制 ===");

    DefaultTransactionDefinition outerDef = new DefaultTransactionDefinition();
    outerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus outerStatus = transactionManager.getTransaction(outerDef);

    try {
      log.info("外层事务开始");
      jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书11");

      DefaultTransactionDefinition innerDef = new DefaultTransactionDefinition();
      innerDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
      TransactionStatus innerStatus = transactionManager.getTransaction(innerDef);

      try {
        log.info("NESTED内层事务开始，是否新事务: {}", innerStatus.isNewTransaction());
        log.info("NESTED内层事务，是否有保存点: {}", innerStatus.hasSavepoint());

        jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书12");

        // 模拟内层事务异常
        throw new RuntimeException("内层事务异常");

      } catch (Exception e) {
        log.error("内层事务异常: {}", e.getMessage());
        transactionManager.rollback(innerStatus);
        log.info("内层事务回滚到保存点");
      }

      // 外层事务继续
      jdbcTemplate.update("INSERT INTO books (title) VALUES (?)", "测试图书13");
      transactionManager.commit(outerStatus);
      log.info("外层事务提交");

      int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE title LIKE '测试图书1%'", Integer.class);
      log.info("最终插入记录数: {} (应该是2，内层事务回滚，外层事务正常)", count);

    } catch (Exception e) {
      log.error("外层事务异常", e);
      transactionManager.rollback(outerStatus);
    }
  }

  @Test
  void testPropagationComparison() {
    log.info("=== 事务传播机制对比总结 ===");
    log.info("1. REQUIRED: 默认机制，有事务加入，无事务新建");
    log.info("2. REQUIRES_NEW: 总是新建事务，挂起父事务");
    log.info("3. SUPPORTS: 有事务加入，无事务非事务运行");
    log.info("4. NOT_SUPPORTED: 总是非事务运行，挂起父事务");
    log.info("5. MANDATORY: 必须有事务，否则抛异常");
    log.info("6. NEVER: 必须无事务，否则抛异常");
    log.info("7. NESTED: 嵌套事务，使用保存点机制");
  }

}