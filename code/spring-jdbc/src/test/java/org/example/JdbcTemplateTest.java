package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.example.model.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {Application.class})
@ExtendWith(SpringExtension.class)
class JdbcTemplateTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplateTest.class);

  @Autowired
  private DataSource dataSource;

  private JdbcTemplate getJdbcTemplate() {
    return new JdbcTemplate(dataSource);
  }

  // RowMapper
  private static final RowMapper<Book> BOOK_ROW_MAPPER = (rs, rowNum) -> {
    Book book = new Book();
    book.setId(rs.getLong("id"));
    book.setTitle(rs.getString("title"));
    book.setAuthor(rs.getString("author"));
    book.setIsbn(rs.getString("isbn"));
    book.setPrice(rs.getBigDecimal("price"));
    book.setPublishDate(rs.getDate("publish_date").toLocalDate());
    book.setCategory(rs.getString("category"));
    book.setStock(rs.getInt("stock"));
    return book;
  };

  @Test
  void testCount() {
    JdbcTemplate jdbcTemplate = getJdbcTemplate();

    String sql = "SELECT COUNT(*) FROM books";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

    LOGGER.info("图书总数: {}", count);
  }

  @Test
  void testSelectAll() {
    JdbcTemplate jdbcTemplate = getJdbcTemplate();

    String sql = "SELECT * FROM books";
    List<Book> books = jdbcTemplate.query(sql, BOOK_ROW_MAPPER);

    LOGGER.info("查询到 {} 本图书", books.size());
    books.forEach(book -> LOGGER.info("图书: {} - {}", book.getTitle(), book.getAuthor()));
  }

  @Test
  void testSelectById() {
    JdbcTemplate jdbcTemplate = getJdbcTemplate();

    String sql = "SELECT * FROM books WHERE id = ?";
    Book book = jdbcTemplate.queryForObject(sql, BOOK_ROW_MAPPER, 1L);

    LOGGER.info("查询图书: {} - {}", book.getTitle(), book.getAuthor());
  }

  @Test
  void testInsert() {
    JdbcTemplate jdbcTemplate = getJdbcTemplate();

    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    int rows = jdbcTemplate.update(sql,
        "测试图书", "测试作者", "978-0-000-00000-0",
        new BigDecimal("99.99"), LocalDate.now(), "测试", 10);

    LOGGER.info("插入记录数: {}", rows);
  }

  @Test
  void testUpdate() {
    JdbcTemplate jdbcTemplate = getJdbcTemplate();

    String sql = "UPDATE books SET price = ?, stock = ? WHERE id = ?";
    int rows = jdbcTemplate.update(sql, new BigDecimal("199.99"), 100, 1L);

    LOGGER.info("更新记录数: {}", rows);
  }

  @Test
  void testDelete() {
    JdbcTemplate jdbcTemplate = getJdbcTemplate();

    String sql = "DELETE FROM books WHERE isbn = ?";
    int rows = jdbcTemplate.update(sql, "978-0-000-00000-0");

    LOGGER.info("删除记录数: {}", rows);
  }
}