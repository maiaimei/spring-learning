package org.example.config;

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

@ContextConfiguration(classes = {DataSourceConfig.class})
@ExtendWith(SpringExtension.class)
class DataSourceConfigTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfigTest.class);

  @Autowired
  private DataSource dataSource;

  @Test
  void testConnection() throws SQLException {
    final Connection connection = dataSource.getConnection();
    LOGGER.info("数据库连接：{}", connection);
  }
}
