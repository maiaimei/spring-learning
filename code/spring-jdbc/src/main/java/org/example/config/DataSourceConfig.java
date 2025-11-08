package org.example.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DataSourceConfig {

  // @Bean
  public DataSource driverManagerDataSource() {
    String dbUrl = """
        jdbc:mysql://localhost:3306/spring_jdbc?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true""";
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

  @Autowired
  private DatabaseProperties databaseProperties;

  /**
   * 用于解析@Value注解中的占位符，必须是静态方法，确保在其他Bean初始化前加载
   */
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public DataSource hikariDataSource() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setDriverClassName(databaseProperties.getDriverClassName());
    dataSource.setJdbcUrl(databaseProperties.getUrl());
    dataSource.setUsername(databaseProperties.getUsername());
    dataSource.setPassword(databaseProperties.getPassword());
    dataSource.setPoolName(databaseProperties.getPoolName());
    dataSource.setConnectionTimeout(databaseProperties.getConnectionTimeout());
    dataSource.setIdleTimeout(databaseProperties.getIdleTimeout());
    dataSource.setMaxLifetime(databaseProperties.getMaxLifetime());
    dataSource.setMaximumPoolSize(databaseProperties.getMaximumPoolSize());
    dataSource.setMinimumIdle(databaseProperties.getMinimumIdle());
    dataSource.setConnectionTestQuery(databaseProperties.getConnectionTestQuery());
    dataSource.setAutoCommit(databaseProperties.isAutoCommit());
    dataSource.setConnectionInitSql(databaseProperties.getConnectionInitSql());
    dataSource.setLeakDetectionThreshold(databaseProperties.getLeakDetectionThreshold());
    return dataSource;
  }

}
