package org.example;

import static org.example.constants.H2Constants.*;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class H2DatabaseConfig {

  @Bean
  public org.h2.tools.Server h2WebServer() throws java.sql.SQLException {
    return org.h2.tools.Server.createWebServer(H2_WEB_SERVER_ARGS).start();
  }

  @Bean
  public org.h2.tools.Server h2TcpServer() throws java.sql.SQLException {
    return org.h2.tools.Server.createTcpServer(H2_TCP_SERVER_ARGS).start();
  }

  @Bean("localDataSource")
  public DataSource localDataSource() {
    HikariConfig config = new HikariConfig();
    config.setDriverClassName(H2_DRIVER_CLASS_NAME);
    config.setJdbcUrl(H2_MEMORY_URL);
    config.setUsername(H2_USERNAME);
    config.setPassword(H2_PASSWORD);
    config.setMaximumPoolSize(1);
    config.setMinimumIdle(1);
    return new HikariDataSource(config);
  }

  @Bean
  @Primary
  @DependsOn({"h2TcpServer", "localDataSource"})
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setDriverClassName(H2_DRIVER_CLASS_NAME);
    config.setJdbcUrl(H2_TCP_URL);
    config.setUsername(H2_USERNAME);
    config.setPassword(H2_PASSWORD);
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(2);
    return new HikariDataSource(config);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

}