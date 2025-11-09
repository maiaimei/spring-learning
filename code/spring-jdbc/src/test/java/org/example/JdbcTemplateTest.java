package org.example;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.example.model.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {Application.class})
@ExtendWith(SpringExtension.class)
class JdbcTemplateTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplateTest.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  // RowMapper
  private static final RowMapper<Book> BOOK_ROW_MAPPER = new RowMapper<Book>() {
    @Override
    public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
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
    }
  };

  @Test
  void testCount() {
    String sql = "SELECT COUNT(*) FROM books";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

    LOGGER.info("图书总数: {}", count);
  }

  @Test
  void testSelectAll() {
    String sql = "SELECT * FROM books";
    List<Book> books = jdbcTemplate.query(sql, BOOK_ROW_MAPPER);

    LOGGER.info("查询到 {} 本图书", books.size());
    books.forEach(book -> LOGGER.info("图书: {} - {}", book.getTitle(), book.getAuthor()));
  }

  @Test
  void testSelectById() {
    String sql = "SELECT * FROM books WHERE id = ?";
    Book book = jdbcTemplate.queryForObject(sql, BOOK_ROW_MAPPER, 1L);

    LOGGER.info("查询图书: {} - {}", book.getTitle(), book.getAuthor());
  }

  @Test
  void testInsert() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
    int rows = jdbcTemplate.update(sql,
        "测试图书", "测试作者", "978-0-000-00000-0",
        new BigDecimal("99.99"), LocalDate.now(), "测试", 10);

    LOGGER.info("插入记录数: {}", rows);
  }

  @Test
  void testUpdate() {
    String sql = "UPDATE books SET price = ?, stock = ? WHERE id = ?";
    int rows = jdbcTemplate.update(sql, new BigDecimal("199.99"), 100, 1L);

    LOGGER.info("更新记录数: {}", rows);
  }

  @Test
  void testDelete() {
    String sql = "DELETE FROM books WHERE isbn = ?";
    int rows = jdbcTemplate.update(sql, "978-0-000-00000-0");

    LOGGER.info("删除记录数: {}", rows);
  }

  @Test
  void testBatchUpdate() {
    // 准备批量更新的数据
    List<Book> booksToInsert = Arrays.asList(
        createBook("批量图书1", "作者1", "978-1-111-11111-1", "99.99", "编程", 20),
        createBook("批量图书2", "作者2", "978-2-222-22222-2", "89.99", "算法", 15),
        createBook("批量图书3", "作者3", "978-3-333-33333-3", "79.99", "数据库", 25));

    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";

    int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        Book book = booksToInsert.get(i);
        ps.setString(1, book.getTitle());
        ps.setString(2, book.getAuthor());
        ps.setString(3, book.getIsbn());
        ps.setBigDecimal(4, book.getPrice());
        ps.setDate(5, java.sql.Date.valueOf(book.getPublishDate()));
        ps.setString(6, book.getCategory());
        ps.setInt(7, book.getStock());
      }

      @Override
      public int getBatchSize() {
        return booksToInsert.size();
      }
    });

    LOGGER.info("批量插入结果: {}", Arrays.toString(results));
    LOGGER.info("成功插入 {} 条记录", results.length);
  }

  @Test
  void testBatchUpdateWithObjectArray() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";

    // 准备批量参数数组
    List<Object[]> batchArgs = new ArrayList<>();
    batchArgs.add(new Object[]{"数组批量图书1", "数组作者1", "978-4-444-44444-4", new BigDecimal("109.99"),
        java.sql.Date.valueOf(LocalDate.now()), "Java", 30});
    batchArgs.add(new Object[]{"数组批量图书2", "数组作者2", "978-5-555-55555-5", new BigDecimal("119.99"),
        java.sql.Date.valueOf(LocalDate.now()), "Spring", 25});
    batchArgs.add(new Object[]{"数组批量图书3", "数组作者3", "978-6-666-66666-6", new BigDecimal("129.99"),
        java.sql.Date.valueOf(LocalDate.now()), "MySQL", 20});

    int[] results = jdbcTemplate.batchUpdate(sql, batchArgs);

    LOGGER.info("Object[]批量插入结果: {}", Arrays.toString(results));
    LOGGER.info("成功插入 {} 条记录", results.length);
  }

  @Test
  void testInsertWithGeneratedKey() {
    String sql = "INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();

    int rows = jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, "获取ID图书");
        ps.setString(2, "获取ID作者");
        ps.setString(3, "978-7-777-77777-7");
        ps.setBigDecimal(4, new BigDecimal("149.99"));
        ps.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
        ps.setString(6, "测试");
        ps.setInt(7, 50);
        return ps;
      }
    }, keyHolder);

    LOGGER.info("插入记录数: {}", rows);

    if (keyHolder.getKey() != null) {
      Long generatedId = keyHolder.getKey().longValue();
      LOGGER.info("自动生成的ID: {}", generatedId);
    }

    // 也可以获取所有生成的键
    LOGGER.info("所有生成的键: {}", keyHolder.getKeys());
  }

  private Book createBook(String title, String author, String isbn, String price, String category, int stock) {
    Book book = new Book();
    book.setTitle(title);
    book.setAuthor(author);
    book.setIsbn(isbn);
    book.setPrice(new BigDecimal(price));
    book.setPublishDate(LocalDate.now());
    book.setCategory(category);
    book.setStock(stock);
    return book;
  }
}