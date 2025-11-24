package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ContextConfiguration(classes = {TestApplication.class})
@ExtendWith(SpringExtension.class)
public class DataSourceTest {

  @Autowired
  private DataSource dataSource;

  @Test
  void testConnection() throws SQLException {
    log.info("数据源类型：{}", dataSource.getClass().getName());
    final Connection connection = dataSource.getConnection();
    log.info("数据库连接：{}", connection);
    connection.close();
    log.info("连接已关闭");
  }
}
