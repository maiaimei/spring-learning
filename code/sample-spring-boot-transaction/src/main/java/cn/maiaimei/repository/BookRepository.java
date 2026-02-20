package cn.maiaimei.repository;

import cn.maiaimei.model.Book;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final RowMapper<Book> bookRowMapper = new RowMapper<Book>() {
    @Override
    public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
      Book book = new Book();
      book.setId(rs.getLong("id"));
      book.setTitle(rs.getString("title"));
      book.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
      return book;
    }
  };

  public List<Book> findAll() {
    String sql = "SELECT id, title, created_at FROM books";
    return jdbcTemplate.query(sql, bookRowMapper);
  }

  public Book findById(Long id) {
    String sql = "SELECT id, title, created_at FROM books WHERE id = ?";
    return jdbcTemplate.queryForObject(sql, bookRowMapper, id);
  }

  public int save(Book book) {
    String sql = "INSERT INTO books (title, created_at) VALUES (?, ?)";
    return jdbcTemplate.update(sql, book.getTitle(), LocalDateTime.now());
  }

  public int update(Book book) {
    String sql = "UPDATE books SET title = ? WHERE id = ?";
    return jdbcTemplate.update(sql, book.getTitle(), book.getId());
  }

  public int deleteById(Long id) {
    String sql = "DELETE FROM books WHERE id = ?";
    return jdbcTemplate.update(sql, id);
  }
}
