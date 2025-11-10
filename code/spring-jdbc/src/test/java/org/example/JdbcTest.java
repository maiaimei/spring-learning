package org.example;

import java.sql.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JdbcTest {

  private static final Logger log = LoggerFactory.getLogger(JdbcTest.class);

  String H2_URL = "jdbc:h2:tcp://localhost:9092/mem:testdb";
  String H2_USERNAME = "sa";
  String H2_PASSWORD = "";

  @Test
  void testCount() {
    // DriverManager会自动扫描classpath，找到所有的JDBC驱动，然后根据我们传入的URL自动挑选一个合适的驱动。
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM books")) {
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            long count = rs.getLong(1); // 注意：索引从1开始
            log.info("数据库中共有 {} 本图书", count);
          }
        }
      }
    } catch (SQLException e) {
      log.error("统计图书数量失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testSelectAll() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
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
      log.error("查询所有图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testSelectById() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
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
      log.error("根据ID查询图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testInsert() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title) VALUES (?)")) {
        ps.setString(1, "测试图书"); // 注意：索引从1开始
        final int affectedRows = ps.executeUpdate();
        log.info("成功插入 {} 本图书", affectedRows);
      }
    } catch (SQLException e) {
      log.error("插入图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testUpdate() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("UPDATE books SET title = ? WHERE id = ?")) {
        ps.setString(1, "Java设计模式");
        ps.setLong(2, 4);
        final int affectedRows = ps.executeUpdate();
        log.info("成功更新 {} 本图书", affectedRows);
      }
    } catch (SQLException e) {
      log.error("更新图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testDelete() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE title LIKE '%测试%'")) {
        final int affectedRows = ps.executeUpdate();
        log.info("成功删除 {} 本图书", affectedRows);
      }
    } catch (SQLException e) {
      log.error("删除图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testBatch() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
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
      log.error("批量插入图书失败 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testTransactionSuccess() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
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
      log.error("事务执行异常 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testTransactionFailed() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
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
      log.error("事务执行异常 - SQL状态: {}, 错误代码: {}, 错误信息: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

}
