package org.example.config;

import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 开启 Spring 定时任务功能
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S") // 启用 ShedLock 调度锁支持。defaultLockAtMostFor：设置默认最大锁定时间（ISO8601 格式）。如果节点宕机，这个时间决定了锁多久后自动释放。
@Configuration
public class ScheduleConfig {

  // 配置 LockProvider 来管理锁
  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    //return new JdbcTemplateLockProvider(dataSource);
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .withTableName("shedlock")
            .usingDbTime() // 使用数据库时间
            .build()
    );
  }
}
