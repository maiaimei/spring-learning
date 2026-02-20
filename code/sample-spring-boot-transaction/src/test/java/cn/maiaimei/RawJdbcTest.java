package cn.maiaimei;

import java.sql.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RawJdbcTest {

  @BeforeAll
  static void loadDriver() {
    try {
      Class.forName(H2_DRIVER_CLASS_NAME);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("H2 driver not found", e);
    }
  }

  @Test
  @Order(1)
  void testCount() {
    // DriverManager会自动扫描classpath，找到所有的JDBC驱动，然后根据我们传入的URL自动挑选一个合适的驱动。
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM books")) {
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            long count = rs.getLong(1); // 注意：索引从1开始
            log.info("数据库中共有 {} 本图书", count);
          }
        }
      }
    } catch (SQLException e) {
      log.error("统计图书数量失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  @Order(2)
  void testSelectAll() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("SELECT id, title FROM books")) {
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            long id = rs.getLong(1); // 注意：索引从1开始
            String title = rs.getString(2);
            log.info("查询到图书 - ID: {}, 书名: {}", id, title);
          }
        }
      }
    } catch (SQLException e) {
      log.error("查询所有图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  @Order(3)
  void testSelectById() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("SELECT id, title FROM books WHERE id = ?")) {
        ps.setLong(1, 1); // 注意：索引从1开始
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            long id = rs.getLong(1); // 注意：索引从1开始
            String title = rs.getString(2);
            log.info("根据ID查询到图书 - ID: {}, 书名: {}", id, title);
          }
        }
      }
    } catch (SQLException e) {
      log.error("根据ID查询图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(),
          e);
    }
  }

  @Test
  @Order(4)
  void testInsert() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title) VALUES (?)")) {
        ps.setString(1, "测试图书"); // 注意：索引从1开始
        final int affectedRows = ps.executeUpdate();
        log.info("成功插入 {} 本图书", affectedRows);
      }
    } catch (SQLException e) {
      log.error("插入图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  @Order(5)
  void testUpdate() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("UPDATE books SET title = ? WHERE id = ?")) {
        ps.setString(1, "Java设计模式");
        ps.setLong(2, 4);
        final int affectedRows = ps.executeUpdate();
        log.info("成功更新 {} 本图书", affectedRows);
      }
    } catch (SQLException e) {
      log.error("更新图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  @Order(6)
  void testDelete() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE title LIKE '%测试%'")) {
        final int affectedRows = ps.executeUpdate();
        log.info("成功删除 {} 本图书", affectedRows);
      }
    } catch (SQLException e) {
      log.error("删除图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  @Order(7)
  void testBatch() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title) VALUES (?)")) {
        for (int i = 1; i <= 5; i++) {
          ps.setString(1, "测试图书" + i);
          ps.addBatch();
        }
        int[] ns = ps.executeBatch();
        for (int n : ns) {
          log.info("批量插入第 {} 本图书成功", n);
        }
      }
    } catch (SQLException e) {
      log.error("批量插入图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  @Order(8)
  void testTransactionSuccess() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      conn.setAutoCommit(false);
      conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title) VALUES (?)")) {
        // 插入第一条记录
        ps.setString(1, "事务测试图书1");
        ps.executeUpdate();
        // 插入第二条记录
        ps.setString(1, "事务测试图书2");
        ps.executeUpdate();
        // 提交事务
        conn.commit();
        log.info("事务提交成功，插入了2本图书");
      } catch (SQLException e) {
        // 发生异常，回滚事务
        conn.rollback();
        log.error("事务执行失败，已回滚所有操作");
        throw e;
      }
    } catch (SQLException e) {
      log.error("事务执行异常 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  @Order(9)
  void testTransactionFailed() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      conn.setAutoCommit(false);
      conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title) VALUES (?)")) {
        // 插入第一条记录
        ps.setString(1, "事务测试图书3");
        ps.executeUpdate();
        // 插入第二条记录，故意传入null，违反了title字段的非空约束，触发异常
        ps.setString(1, null);
        ps.executeUpdate();
        // 如果没有异常发生，提交事务
        conn.commit();
        log.info("事务提交成功（不应该执行到这里）");
      } catch (SQLException e) {
        // 发生异常，回滚事务
        conn.rollback();
        log.error("检测到异常，事务已回滚");
        throw e;
      }
    } catch (SQLException e) {
      log.error("事务执行异常 - SQL状态: {}, 错误代码: {}, 错误信息: {}", e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  @Order(10)
  void testCheckTransactionIsolation() {
    try (Connection conn = DriverManager.getConnection(H2_TCP_URL, H2_USERNAME, H2_PASSWORD)) {
      final int transactionIsolation = conn.getTransactionIsolation();
      switch (transactionIsolation) {
        case Connection.TRANSACTION_NONE:
          log.info("当前数据库事务隔离级别: TRANSACTION_NONE (不支持事务)");
          break;
        case Connection.TRANSACTION_READ_UNCOMMITTED:
          log.info("当前数据库事务隔离级别: TRANSACTION_READ_UNCOMMITTED (读未提交)");
          break;
        case Connection.TRANSACTION_READ_COMMITTED:
          log.info("当前数据库事务隔离级别: TRANSACTION_READ_COMMITTED (读已提交)");
          break;
        case Connection.TRANSACTION_REPEATABLE_READ:
          log.info("当前数据库事务隔离级别: TRANSACTION_REPEATABLE_READ (可重复读)");
          break;
        case Connection.TRANSACTION_SERIALIZABLE:
          log.info("当前数据库事务隔离级别: TRANSACTION_SERIALIZABLE (可串行化)");
          break;
        default:
          log.info("当前数据库事务隔离级别: 未知");
          break;
      }
    } catch (SQLException e) {
      log.error("获取数据库事务隔离级别异常 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

}