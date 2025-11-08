# Spring JDBC

## 数据库连接

数据库连接配置，从系统属性或环境变量读取配置信息：

配置优先级：

1. 系统属性（`-Ddb.url`、`-Ddb.username`、`-Ddb.password`）

2. 环境变量（`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`）

3. 默认值


使用方式：

- 系统属性：`java -Ddb.username=myuser -Ddb.password=mypass MyApp`

- 环境变量：`export DB_USERNAME=myuser && export DB_PASSWORD=mypass`
- IDEA环境变量：`DB_URL=jdbc:mysql://localhost:3306/spring_jdbc?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true;DB_USERNAME=root;DB_PASSWORD=`

## 单元测试

Spring测试相关的注解，包括：

`@ContextConfiguration` - 配置测试上下文

`@SpringJUnitConfig` - JUnit 5集成

`@TestPropertySource` - 测试属性配置

`@DirtiesContext` - 上下文清理 等Spring测试功能。

