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
            log.info("Books count: {}", count);
          }
        }
      }
    } catch (SQLException e) {
      log.error("Failed to count books - SQL State: {}, Error Code: {}, Message: {}",
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
            log.info("ID: {}, Title: {}", id, title);
          }
        }
      }
    } catch (SQLException e) {
      log.error("Failed to select all books - SQL State: {}, Error Code: {}, Message: {}",
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
            log.info("ID: {}, Title: {}", id, title);
          }
        }
      }
    } catch (SQLException e) {
      log.error("Failed to select book by ID - SQL State: {}, Error Code: {}, Message: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testInsert() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title) VALUES (?)")) {
        ps.setString(1, "测试图书"); // 注意：索引从1开始
        final int affectedRows = ps.executeUpdate();
        log.info("Inserted books: {}", affectedRows);
      }
    } catch (SQLException e) {
      log.error("Failed to insert book - SQL State: {}, Error Code: {}, Message: {}",
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
        log.info("Updated books: {}", affectedRows);
      }
    } catch (SQLException e) {
      log.error("Failed to insert book - SQL State: {}, Error Code: {}, Message: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testDelete() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
      try (PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE title LIKE '测试%'")) {
        final int affectedRows = ps.executeUpdate();
        log.info("Deleted books: {}", affectedRows);
      }
    } catch (SQLException e) {
      log.error("Failed to delete books - SQL State: {}, Error Code: {}, Message: {}",
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
          log.info("{} inserted.", n);
        }
      }
    } catch (SQLException e) {
      log.error("Failed to insert book - SQL State: {}, Error Code: {}, Message: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

  @Test
  void testTransaction() {
    try (Connection conn = DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD)) {
      conn.setAutoCommit(false);
      conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title) VALUES (?)")) {

      }
    } catch (SQLException e) {
      log.error("Failed to insert book - SQL State: {}, Error Code: {}, Message: {}",
          e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
    }
  }

}
