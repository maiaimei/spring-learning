package cn.maiaimei.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DataSourceConfig {

  @Bean
  public DataSource driverManagerDataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(); // 驱动数据源
    dataSource.setDriverClassName(H2_DRIVER_CLASS_NAME); // 驱动配置
    dataSource.setUrl(H2_TCP_URL);
    dataSource.setUsername(H2_USERNAME);
    dataSource.setPassword(H2_PASSWORD);
    return dataSource;
  }

  @Primary
  @Bean
  public DataSource hikariDataSource() {
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
    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    jdbcTemplate.setDataSource(dataSource);
    return jdbcTemplate;
  }

  @Bean
  public PlatformTransactionManager transactionManager(DataSource dataSource) {
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
    transactionManager.setDataSource(dataSource);
    return transactionManager;
  }

}
