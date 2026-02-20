# Spring Boot

## 时区配置

Spring Boot 以 UTC 时间启动程序推荐使用 JVM 参数。

IDE 运行配置中添加：

```bash
-Duser.timezone=UTC
```

启动脚本中添加：

```bash
java -Duser.timezone=UTC -jar app.jar
```

## 日志配置

### Logback 日志配置

[https://logback.qos.ch/manual/layouts.html](https://logback.qos.ch/manual/layouts.html)

#### Trace ID 配置

在日志模式中添加 `%X{traceId:-}` 以显示 MDC 中的 traceId，建议放在线程名之后、logger 名之前。
