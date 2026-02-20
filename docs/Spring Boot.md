# Spring Boot

## 自定义配置

让 application.yml 自动识别自定义配置的步骤：

1. 添加 spring-boot-configuration-processor 依赖
2. 编译项目生成元数据文件
3. IDE 读取元数据提供自动完成

配置元数据文件位置：

* 源码模块： target/classes/META-INF/spring-configuration-metadata.json
* JAR 包内： META-INF/spring-configuration-metadata.json

注意：如果自定义配置无法自动识别，可能是 IDE 缓存问题，尝试重启 IDE 后重新构建项目，确认配置元数据文件位置已存在，IDE 就能识别配置并提供自动完成和跳转功能。

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
