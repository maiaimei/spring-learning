package org.example.component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskSchedulerManager {

  /**
   * @SchedulerLock 分布式锁注解，确保多实例环境下同一时刻只有一个实例执行该任务
   * name: 锁的唯一标识，唯一标识该任务的名称，建议使用类名+方法名组合，对应数据库shedlock表的name字段
   * lockAtLeastFor: 最小锁定时间，锁的最短持有时间(PT1M=1分钟)，防止任务执行过快导致重复执行，避免任务频繁触发
   * lockAtMostFor: 最大锁定时间，锁的最长持有时间(PT1M=1分钟)，防止节点崩溃导致锁无法释放，防止死锁
   */
  @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
  @SchedulerLock(name = "TaskSchedulerManager_scheduledTask", lockAtLeastFor = "PT2M", lockAtMostFor = "PT4M") // 至少锁定 2 分钟（PT2M），最多锁定 4 分钟（PT4M）
  public void scheduledTask() {
    System.out.println("执行定时任务");
  }
}