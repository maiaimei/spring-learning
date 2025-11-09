package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {Application.class})
@ExtendWith(SpringExtension.class)
class DataSourceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceTest.class);

  @Autowired
  private DataSource dataSource;

  @Test
  void testConnection() throws SQLException {
    LOGGER.info("DataSource类型：{}", dataSource.getClass().getName());
    final Connection connection = dataSource.getConnection();
    LOGGER.info("数据库连接：{}", connection);
    connection.close();
    LOGGER.info("连接已关闭");
  }
}
