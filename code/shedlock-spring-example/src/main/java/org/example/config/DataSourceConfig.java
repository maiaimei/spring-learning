package org.example.config;

import static org.example.constants.H2Constants.*;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DataSourceConfig {

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(); // 驱动数据源
    dataSource.setDriverClassName(H2_DRIVER_CLASS_NAME); // 驱动配置
    dataSource.setUrl(H2_TCP_URL);
    dataSource.setUsername(H2_USERNAME);
    dataSource.setPassword(H2_PASSWORD);
    return dataSource;
  }
}
