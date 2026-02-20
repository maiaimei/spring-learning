package cn.maiaimei;

import cn.maiaimei.model.Book;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

@Slf4j
@ContextConfiguration(classes = {TestApplication.class})
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdbcTemplateTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  // RowMapper
  private static final RowMapper<Book> BOOK_ROW_MAPPER = new RowMapper<Book>() {
    @Override
    public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
      Book book = new Book();
      book.setId(rs.getLong("id"));
      book.setTitle(rs.getString("title"));
      return book;
    }
  };

  @Test
  @Order(1)
  void testCount() {
    String sql = "SELECT COUNT(*) FROM books";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

    log.info("图书总数: {}", count);
  }

  @Test
  @Order(2)
  void testSelectAll() {
    String sql = "SELECT * FROM books";
    List<Book> books = jdbcTemplate.query(sql, BOOK_ROW_MAPPER);

    log.info("查询到 {} 本图书", books.size());
    books.forEach(book -> log.info("图书: ID={}, 标题={}", book.getId(), book.getTitle()));
  }

  @Test
  @Order(3)
  void testSelectById() {
    String sql = "SELECT * FROM books WHERE id = ?";
    Book book = jdbcTemplate.queryForObject(sql, BOOK_ROW_MAPPER, 1L);
    Assert.notNull(book, "找不到ID等于1的图书");
    log.info("查询图书: ID={}, 标题={}", book.getId(), book.getTitle());
  }

  @Test
  @Order(4)
  void testInsert() {
    String sql = "INSERT INTO books (title) VALUES (?)";
    int rows = jdbcTemplate.update(sql, "测试图书");

    log.info("插入记录数: {}", rows);
  }

  @Test
  @Order(5)
  void testUpdate() {
    String sql = "UPDATE books SET title = ? WHERE id = ?";
    int rows = jdbcTemplate.update(sql, "更新后的图书标题", 1L);

    log.info("更新记录数: {}", rows);
  }

  @Test
  @Order(6)
  void testDelete() {
    String sql = "DELETE FROM books WHERE title LIKE ?";
    int rows = jdbcTemplate.update(sql, "%测试%");

    log.info("删除记录数: {}", rows);
  }

  @Test
  @Order(7)
  void testBatchUpdate() {
    // 准备批量更新的数据
    List<Book> booksToInsert = Arrays.asList(
        createBook("测试批量插入图书1"),
        createBook("测试批量插入批量图书2"),
        createBook("测试批量插入批量图书3"));

    String sql = "INSERT INTO books (title) VALUES (?)";

    int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        Book book = booksToInsert.get(i);
        ps.setString(1, book.getTitle());
      }

      @Override
      public int getBatchSize() {
        return booksToInsert.size();
      }
    });

    log.info("批量插入结果: {}", Arrays.toString(results));
    log.info("成功插入 {} 条记录", results.length);
  }

  @Test
  @Order(8)
  void testBatchUpdateWithObjectArray() {
    String sql = "INSERT INTO books (title) VALUES (?)";

    // 准备批量参数数组
    Object[][] batchArgs = {
        {"测试数组批量插入图书1"},
        {"测试数组批量插入图书2"},
        {"测试数组批量插入图书3"}
    };

    int[] results = jdbcTemplate.batchUpdate(sql, Arrays.asList(batchArgs));

    log.info("批量插入结果: {}", Arrays.toString(results));
    log.info("成功插入 {} 条记录", results.length);
  }

  @Test
  @Order(9)
  void testInsertWithGeneratedKey() {
    String sql = "INSERT INTO books (title) VALUES (?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();

    int rows = jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, "获取ID图书");
        return ps;
      }
    }, keyHolder);

    log.info("插入记录数: {}", rows);

    // 获取生成的ID - 使用getKeyAs方法指定字段名
    if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
      // 从keys map中获取ID字段
      Object idValue = keyHolder.getKeys().get("ID");
      log.info("从keys map获取的ID: {}", idValue);
    }

    // 获取所有生成的键
    log.info("所有生成的键: {}", keyHolder.getKeys());
  }

  private Book createBook(String title) {
    Book book = new Book();
    book.setTitle(title);
    return book;
  }
}