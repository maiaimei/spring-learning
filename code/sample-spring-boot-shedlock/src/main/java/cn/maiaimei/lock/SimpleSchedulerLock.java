package cn.maiaimei.lock;

import java.time.Duration;
import java.time.Instant;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 简单的基于数据库的分布式锁实现
 * 演示 lock_until 的更新逻辑
 */
public class SimpleSchedulerLock {

  private final JdbcTemplate jdbcTemplate;
  private final String hostName;

  public SimpleSchedulerLock(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.hostName = getHostName();
  }

  /**
   * 执行带锁的任务
   */
  public void executeWithLock(String lockName, Duration lockAtLeastFor, Duration lockAtMostFor,
      Runnable task) {
    Instant now = Instant.now();

    // 步骤1：尝试获取锁
    if (tryLock(lockName, now, lockAtMostFor)) {
      try {
        Instant taskStartTime = Instant.now();
        // 步骤2：执行任务
        task.run();
        // 步骤3：释放锁
        releaseLock(lockName, taskStartTime, lockAtLeastFor);
      } catch (Exception e) {
        releaseLock(lockName, now, lockAtLeastFor);
        throw e;
      }
    }
  }

  /**
   * 获取锁：lock_until = now + lockAtMostFor
   */
  private boolean tryLock(String lockName, Instant now, Duration lockAtMostFor) {
    Instant lockUntil = now.plus(lockAtMostFor);

    String sql = """
        UPDATE shedlock 
        SET lock_until = ?, locked_at = ?, locked_by = ? 
        WHERE name = ? AND lock_until <= ?
        """;

    int updated = jdbcTemplate.update(sql, lockUntil, now, hostName, lockName, now);

    if (updated == 0) {
      try {
        jdbcTemplate.update("INSERT INTO shedlock (name, lock_until, locked_at, locked_by) VALUES (?, ?, ?, ?)",
            lockName, lockUntil, now, hostName);
        return true;
      } catch (Exception e) {
        return false;
      }
    }
    return updated > 0;
  }

  /**
   * 释放锁：lock_until = max(taskStartTime + lockAtLeastFor, now)
   * <p>
   * 场景1：任务快(30秒)，lockAtLeastFor=2分钟
   * unlockTime = max(10:02:00, 10:00:30) = 10:02:00 → 锁持有2分钟
   * <p>
   * 场景2：任务慢(3分钟)，lockAtLeastFor=2分钟
   * unlockTime = max(10:02:00, 10:03:00) = 10:03:00 → 锁持有3分钟
   */
  private void releaseLock(String lockName, Instant taskStartTime, Duration lockAtLeastFor) {
    Instant now = Instant.now();
    Instant minUnlockTime = taskStartTime.plus(lockAtLeastFor);
    Instant unlockTime = minUnlockTime.isAfter(now) ? minUnlockTime : now;

    jdbcTemplate.update("UPDATE shedlock SET lock_until = ? WHERE name = ?", unlockTime, lockName);
  }

  private String getHostName() {
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      return "unknown";
    }
  }
}

/*
 * lock_until 更新逻辑总结：
 *
 * 1. 获取锁时：lock_until = now + lockAtMostFor
 *    示例：now=10:00:00, lockAtMostFor=PT4M → lock_until=10:04:00
 *
 * 2. 释放锁时：lock_until = max(taskStartTime + lockAtLeastFor, now)
 *    - 任务快：max(10:02:00, 10:00:30) = 10:02:00 (保证最少2分钟)
 *    - 任务慢：max(10:02:00, 10:03:00) = 10:03:00 (实际执行时间)
 *
 * 3. 节点崩溃：lock_until保持10:04:00，最多4分钟后自动过期
 */
