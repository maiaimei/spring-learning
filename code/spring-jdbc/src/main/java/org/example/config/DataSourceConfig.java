package org.example.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configurable
public class DataSourceConfig {

  @Bean
  public DataSource dataSource() {
    String dbUrl = "jdbc:mysql://localhost:3306/spring_jdbc?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true";
    String dbUsername = "root";
    String dbPassword = "";

    DriverManagerDataSource dataSource = new DriverManagerDataSource(); // 驱动数据源
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); // 驱动配置
    dataSource.setUrl(getProperty("db.url", "DB_URL", dbUrl));
    dataSource.setUsername(getProperty("db.username", "DB_USERNAME", dbUsername));
    dataSource.setPassword(getProperty("db.password", "DB_PASSWORD", dbPassword));
    return dataSource;
  }

  private String getProperty(String systemProperty, String envVariable, String defaultValue) {
    return System.getProperty(systemProperty,
        System.getenv().getOrDefault(envVariable, defaultValue));
  }
}
