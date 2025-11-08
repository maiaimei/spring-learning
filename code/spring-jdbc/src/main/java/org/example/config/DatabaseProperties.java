package org.example.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource("classpath:config/database.properties")
public class DatabaseProperties {

  @Value("${db.driverClassName}")
  private String driverClassName;

  @Value("${db.url}")
  private String url;

  @Value("${db.username}")
  private String username;

  @Value("${db.password}")
  private String password;

  @Value("${db.poolName}")
  private String poolName;

  @Value("${db.connectionTimeout}")
  private long connectionTimeout;

  @Value("${db.idleTimeout}")
  private long idleTimeout;

  @Value("${db.maxLifetime}")
  private long maxLifetime;

  @Value("${db.maximumPoolSize}")
  private int maximumPoolSize;

  @Value("${db.minimumIdle}")
  private int minimumIdle;

  @Value("${db.connectionTestQuery}")
  private String connectionTestQuery;

  @Value("${db.autoCommit}")
  private boolean autoCommit;

  @Value("${db.connectionInitSql}")
  private String connectionInitSql;

  @Value("${db.leakDetectionThreshold}")
  private long leakDetectionThreshold;
}