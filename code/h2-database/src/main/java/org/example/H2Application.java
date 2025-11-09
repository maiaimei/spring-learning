package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class H2Application {

  private static final Logger log = LoggerFactory.getLogger(H2Application.class);

  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(H2DataSourceConfig.class, H2DatabaseInitializer.class);
    context.refresh();

    try {
      JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);

      // 初始化数据库
      log.info("初始化H2数据库...");
      jdbcTemplate.execute(
          "CREATE TABLE IF NOT EXISTS books (" +
              "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
              "title VARCHAR(255) NOT NULL," +
              "author VARCHAR(255) NOT NULL," +
              "price DECIMAL(10,2) NOT NULL," +
              "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
              ")"
      );

      jdbcTemplate.update(
          "INSERT INTO books (title, author, price) VALUES (?, ?, ?)",
          "Spring实战", "张三", 59.99
      );

      jdbcTemplate.update(
          "INSERT INTO books (title, author, price) VALUES (?, ?, ?)",
          "Java核心技术", "李四", 89.99
      );
      log.info("H2数据库初始化完成");

      // 输出H2控制台访问信息
      log.info("===========================================");
      log.info("H2数据库控制台已启动");
      log.info("访问地址: http://localhost:8082");
      log.info("JDBC URL: jdbc:h2:mem:testdb");
      log.info("用户名: root");
      log.info("密码: (空)");
      log.info("===========================================");

      // 查询所有书籍
      log.info("查询所有书籍:");
      jdbcTemplate.query("SELECT * FROM books", rs -> {
        log.info("ID: {}, 标题: {}, 作者: {}, 价格: {}",
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getBigDecimal("price"));
      });

      log.info("H2数据库服务已启动...");

      // 保持应用运行
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        log.info("正在关闭H2数据库服务...");
        context.close();
        log.info("H2数据库服务已关闭");
      }));

      // 保持主线程运行
      try {
        Thread.currentThread().join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.info("应用被中断");
      }
    } catch (Exception e) {
      log.error("应用启动失败", e);
      context.close();
    }
  }
}