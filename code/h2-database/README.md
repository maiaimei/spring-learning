# H2 Database 模块

这个模块提供了H2内存数据库的配置和初始化，用于为Spring JDBC模块提供数据库支持。

## 功能特性

- 配置H2内存数据库
- 使用HikariCP连接池
- 自动初始化数据库表和测试数据
- 提供JdbcTemplate Bean

## 使用方法

### 1. 添加依赖

在需要使用H2数据库的模块的pom.xml中添加：

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>h2-database</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 导入配置

在Spring配置类中导入H2配置：

```java

@Configuration
@Import({H2DataSourceConfig.class, H2DatabaseInitializer.class})
public class AppConfig {
  // 其他配置
}
```

### 3. 使用JdbcTemplate

```java

@Autowired
private JdbcTemplate jdbcTemplate;

public void queryBooks() {
  jdbcTemplate.query("SELECT * FROM books", rs -> {
    // 处理结果
  });
}
```

## 数据库配置

- 数据库URL: `jdbc:h2:mem:testdb`
- 用户名: `sa`
- 密码: 空
- 连接池: HikariCP (最大10个连接，最小2个空闲连接)

## 预置数据

模块会自动创建`books`表并插入测试数据：

- Spring实战 - 张三 - ¥59.99
- Java核心技术 - 李四 - ¥89.99

## 运行示例

```bash
cd h2-database
mvn compile exec:java -Dexec.mainClass="org.example.H2Application"
```