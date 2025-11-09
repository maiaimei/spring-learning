package org.example;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@DependsOn("jdbcTemplate")
public class H2DatabaseInitializer {

  private static final Logger log = LoggerFactory.getLogger(H2DatabaseInitializer.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @PostConstruct
  public void initDatabase() {
    log.info("初始化H2数据库...");

    // 创建books表
    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS books (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "title VARCHAR(255) NOT NULL," +
            "author VARCHAR(255) NOT NULL," +
            "price DECIMAL(10,2) NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
    );

    // 插入测试数据
    jdbcTemplate.update(
        "INSERT INTO books (title, author, price) VALUES (?, ?, ?)",
        "Spring实战", "张三", 59.99
    );

    jdbcTemplate.update(
        "INSERT INTO books (title, author, price) VALUES (?, ?, ?)",
        "Java核心技术", "李四", 89.99
    );

    log.info("H2数据库初始化完成");
  }
}