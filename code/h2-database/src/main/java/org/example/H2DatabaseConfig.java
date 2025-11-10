package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class H2DatabaseConfig {

  @Bean
  public org.h2.tools.Server h2WebServer() throws java.sql.SQLException {
    return org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
  }

  @Bean
  public org.h2.tools.Server h2TcpServer() throws java.sql.SQLException {
    return org.h2.tools.Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start();
  }

  @Bean("localDataSource")
  public DataSource localDataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    config.setDriverClassName("org.h2.Driver");
    config.setUsername("sa");
    config.setPassword("");
    config.setMaximumPoolSize(1);
    config.setMinimumIdle(1);
    return new HikariDataSource(config);
  }

  @Bean
  @Primary
  @DependsOn({"h2TcpServer", "localDataSource"})
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:tcp://localhost:9092/mem:testdb");
    config.setDriverClassName("org.h2.Driver");
    config.setUsername("sa");
    config.setPassword("");
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(2);
    return new HikariDataSource(config);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

}