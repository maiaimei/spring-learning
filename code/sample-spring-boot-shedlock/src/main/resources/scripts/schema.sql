-- 分布式锁表，用于ShedLock实现定时任务的分布式锁
CREATE TABLE shedlock (
  name VARCHAR(64) COMMENT '锁名称，对应方法名或任务名，唯一标识一个定时任务',
  lock_until TIMESTAMP(3) NULL COMMENT '锁的过期时间，锁持有到此时间',
  locked_at TIMESTAMP(3) NULL COMMENT '锁的获取时间',
  locked_by VARCHAR(255) COMMENT '锁的持有者，通常是主机名或应用实例标识',
  PRIMARY KEY (name)
)