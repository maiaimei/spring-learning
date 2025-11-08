# 事务管理基础

所有Java数据库操作最终都要通过JDBC来实现。

## 什么是JDBC？

JDBC是Java DataBase Connectivity的缩写，它是Java程序访问数据库的标准接口。

使用Java程序访问数据库时，Java代码并不是直接通过TCP连接去访问数据库，而是通过JDBC接口来访问，而JDBC接口则通过JDBC驱动来实现真正对数据库的访问。

|                                        |                                                              |
| -------------------------------------- | ------------------------------------------------------------ |
| ![](./images/JDBC-20251108-095623.png) | JDBC接口是Java标准库自带的，而具体的JDBC驱动是由数据库厂商提供的。 |
| ![](./images/JDBC-20251108-095836.png) | Java标准库自带的JDBC接口其实就是定义了一组接口，而某个具体的JDBC驱动其实就是实现了这些接口的类。 |
| ![](./images/JDBC-20251108-100017.png) |                                                              |

例如，我们在Java代码中如果要访问MySQL，那么必须编写代码操作JDBC接口。注意到JDBC接口是Java标准库自带的，所以可以直接编译。而具体的JDBC驱动是由数据库厂商提供的，例如，MySQL的JDBC驱动由Oracle提供。因此，访问某个具体的数据库，我们只需要引入该厂商提供的JDBC驱动，就可以通过JDBC接口来访问，这样保证了Java程序编写的是一套数据库访问代码，却可以访问各种不同的数据库，因为他们都提供了标准的JDBC驱动：

从代码来看，Java标准库自带的JDBC接口其实就是定义了一组接口，而某个具体的JDBC驱动其实就是实现了这些接口的类：

实际上，一个MySQL的JDBC的驱动就是一个jar包，它本身也是纯Java编写的。我们自己编写的代码只需要引用Java标准库提供的java.sql包下面的相关接口，由此再间接地通过MySQL驱动的jar包通过网络访问MySQL服务器，所有复杂的网络通讯都被封装到JDBC驱动中，因此，Java程序本身只需要引入一个MySQL驱动的jar包就可以正常访问MySQL服务器：

## 什么是事务？

简单来说，事务就是把多个数据库操作打包成一个整体，要么全部成功，要么全部失败。

比如银行转账：从A账户扣钱 + 给B账户加钱，这两步必须同时成功或同时失败，不能出现扣了A的钱但B没收到的情况。

## JDBC事务控制

JDBC的事务代码：

```java
Connection conn = openConnection();
try {
    // 关闭自动提交，默认有“隐式事务”，总是处于“自动提交”模式，也就是每执行一条SQL都是作为事务自动执行的。
    conn.setAutoCommit(false);
    // 设定隔离级别，如果没有设定隔离级别，会使用数据库的默认隔离级别。
    conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    // 执行多条SQL语句
    insert(); update(); delete();
    // 提交事务
    conn.commit();
} catch (SQLException e) {
    // 回滚事务
    conn.rollback();
} finally {
    conn.setAutoCommit(true);
    conn.close();
}
```

**什么时候需要事务？**

只有当你需要同时执行多个相关的**数据库写操作**时，才需要使用事务。具体来说：

**需要事务的场景（涉及写操作）：**

- 转账（扣钱+加钱）- 两次UPDATE操作
- 下单（减库存+创建订单+生成物流单）- 多次INSERT/UPDATE操作
- 用户注册（创建用户+初始化账户+记录日志）- 多次INSERT操作

**不需要事务的场景：**

- 单纯的查询操作（SELECT）
- 单个写操作（单次INSERT/UPDATE/DELETE）
- 多个独立的读操作

## ACID事务特性

事务有4个重要特性，简称ACID：

### 1. 原子性（Atomicity）

**要么全做，要么全不做**

- 就像原子一样不可分割
- 转账时，扣钱和加钱必须同时成功或同时失败
- 出错时会回滚到事务开始前的状态

### 2. 一致性（Consistency）

**数据始终保持正确状态**

- 转账前后，从A账户扣钱 + 给B账户加钱，总金额不变
- 不会出现数据不合理的情况

### 3. 隔离性（Isolation）

**多个事务互不干扰**

- 在并发环境访问下才会存在的问题
- 就像每个事务都在独立的房间里执行
- A用户转账时，不会被B用户的操作影响

### 4. 持久性（Durability）

**提交后永久保存**

- 事务完成后，数据会永久保存在数据库中
- 即使系统崩溃，数据也不会丢失

# Spring事务基础

**`spring-tx`** JAR 提供Spring事务管理的核心功能：

- **声明式事务管理** - 使用 `@Transactional` 注解
- **编程式事务管理** - 通过 `TransactionTemplate` 手动控制事务
- **事务抽象层** - 为不同事务管理器（JPA、JDBC、JMS等）提供统一API
- **事务传播机制** - 控制方法调用时事务的行为
- **事务隔离级别** - 支持多种数据库隔离级别
- **回滚规则** - 灵活的异常回滚配置

Maven依赖：

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-tx</artifactId>
</dependency>
```

**注意**：使用Spring Boot时，该依赖通常通过 `spring-boot-starter-jdbc`、`spring-boot-starter-data-jpa` 等starter自动包含。

[https://docs.spring.io/spring-framework/reference/data-access/transaction.html](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)

# Spring声明式事务

**`@Transactional`** 是Spring框架提供的声明式事务管理注解，用于自动将方法或类包装在数据库事务中。

## 注解定义

```java
package org.springframework.transaction.annotation;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface Transactional {
    
    @AliasFor("transactionManager")
  String value() default "";
    @AliasFor("value")
  String transactionManager() default "";  
    
    String[] label() default {}; 
    
    Propagation propagation() default Propagation.REQUIRED; 
    
    Isolation isolation() default Isolation.DEFAULT;
    
    int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;
    String timeoutString() default "";
    
    boolean readOnly() default false;
    
    Class<? extends Throwable>[] rollbackFor() default {};
    String[] rollbackForClassName() default {};
    
    Class<? extends Throwable>[] noRollbackFor() default {};
    String[] noRollbackForClassName() default {};
}
```

## 注解属性详解

### 事务管理器

```java
// 指定事务管理器
@Transactional("customTransactionManager")
public void method1() { }

// 等价写法
@Transactional(transactionManager = "customTransactionManager")
public void method2() { }
```

### 事务传播机制

**事务传播机制核心在于控制事务的边界和多个事务方法之间的协作关系。**例如，当方法A（已开启事务）调用方法B（也需要事务管理）时，方法B是继续使用A的事务，还是自己新开一个事务，或是其他处理方式。

#### Propagation 枚举值

|      | 传播行为          | 说明                                                         | 使用场景               |
| ---- | ----------------- | ------------------------------------------------------------ | ---------------------- |
| 1    | **REQUIRED**      | Support a current transaction, create a new one if none exists. This is the default setting of a transaction annotation.<br />默认值。如果当前没有事务，就新建一个事务；如果已经存在事务，则加入该事务。 | 大多数业务方法         |
| 2    | **REQUIRES_NEW**  | Create a new transaction, and suspend the current transaction if one exists. <br />总是新建事务，如果当前存在事务，就把当前事务挂起。 | 独立的日志记录、审计   |
| 3    | **SUPPORTS**      | Support a current transaction, execute non-transactionally if none exists. <br />支持当前事务，如果当前没有事务，就以非事务方式执行。 | 查询方法               |
| 4    | **NOT_SUPPORTED** | Execute non-transactionally, suspend the current transaction if one exists. <br />以非事务方式执行，如果当前存在事务，就把当前事务挂起。 | 不需要事务的操作       |
| 5    | **MANDATORY**     | Support a current transaction, throw an exception if none exists. <br />使用当前的事务，如果当前没有事务，就抛出异常。 | 强制事务上下文的方法   |
| 6    | **NEVER**         | Execute non-transactionally, throw an exception if a transaction exists. <br />以非事务方式执行，如果当前存在事务，则抛出异常。 | 绝对不能在事务中的操作 |
| 7    | **NESTED**        | Execute within a nested transaction if a current transaction exists, behave like REQUIRED otherwise.<br />如果当前存在事务，则在嵌套事务内执行；如果当前没有事务，等同于REQUIRED | 部分回滚场景           |

#### 示例代码

```java
@Service
public class OrderService {
    
    @Transactional  // REQUIRED（默认）
    public void createOrder(Order order) {
        orderRepository.save(order);
        // 调用其他事务方法
        auditService.logOrderCreation(order);
        inventoryService.updateStock(order);
    }
}

@Service
public class AuditService {
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderCreation(Order order) {
        // 独立事务，即使主事务回滚，日志也会保存
        auditRepository.save(new AuditLog(order));
    }
}

@Service
public class InventoryService {
    
    @Transactional(propagation = Propagation.NESTED)
    public void updateStock(Order order) {
        // 嵌套事务，可以独立回滚
        for (OrderItem item : order.getItems()) {
            stockRepository.decreaseStock(item.getProductId(), item.getQuantity());
        }
    }
}
```

### 事务隔离级别

**事务隔离级别是在并发环境访问下才会存在的问题。**在实际项目开发中，很可能有两个或多个不同的线程，每个线程拥有各自的数据库事务，要进行同一条数据的更新操作。为了保证数据在更新时候的正确性，那么就需要对数据的同步进行有效的管理，这就属于数据库隔离级别的概念了。

#### Isolation 枚举值

| 隔离级别             | 说明                   | 解决的问题             | 可能出现的问题         |
| -------------------- | ---------------------- | ---------------------- | ---------------------- |
| **DEFAULT**          | 使用数据库默认隔离级别 | -                      | 取决于数据库           |
| **READ_UNCOMMITTED** | 读未提交               | -                      | 脏读、不可重复读、幻读 |
| **READ_COMMITTED**   | 读已提交               | 脏读                   | 不可重复读、幻读       |
| **REPEATABLE_READ**  | 可重复读               | 脏读、不可重复读       | 幻读                   |
| **SERIALIZABLE**     | 串行化                 | 脏读、不可重复读、幻读 | 性能最低               |

#### 示例代码

```java
@Service
public class AccountService {
    
    // 防止脏读
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BigDecimal getBalance(Long accountId) {
        return accountRepository.findById(accountId).getBalance();
    }
    
    // 防止不可重复读
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId);
        Account to = accountRepository.findById(toId);
        
        // 在事务期间，账户余额不会被其他事务修改
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        
        accountRepository.save(from);
        accountRepository.save(to);
    }
    
    // 最高隔离级别
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void criticalOperation() {
        // 完全串行化执行，避免所有并发问题
    }
}
```

### 超时设置

```java
// 设置超时时间（秒）
@Transactional(timeout = 30)
public void longRunningOperation() { }

// 使用字符串形式（支持SpEL表达式）
@Transactional(timeoutString = "${app.transaction.timeout}")
public void configurableTimeout() { }
```

### 只读事务

```java
@Transactional(readOnly = true)
public User findUser(Long id) {
    // 只读事务，优化性能，防止意外修改
    return userRepository.findById(id);
}
```

### 回滚规则

#### 默认回滚行为

- **运行时异常**（RuntimeException及其子类）：自动回滚
- **检查异常**（Exception及其子类，除RuntimeException）：不回滚
- **错误**（Error及其子类）：自动回滚

#### 自定义回滚规则

```java
@Service
public class UserService {
    
    // 指定特定异常回滚
    @Transactional(rollbackFor = {IOException.class, SQLException.class})
    public void createUserWithFile(User user, File file) throws IOException {
        userRepository.save(user);
        fileService.uploadFile(file);  // IOException会触发回滚
    }
    
    // 指定异常不回滚
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
            // 不会回滚事务
        }
        userRepository.save(user);
    }
    
    // 使用类名字符串
    @Transactional(
        rollbackForClassName = {"java.io.IOException", "java.sql.SQLException"},
        noRollbackForClassName = {"java.lang.IllegalArgumentException"}
    )
    public void complexOperation() {
        // 复杂的回滚规则
    }
}
```

#### 编程式回滚

```java
@Service
public class OrderService {
    
    @Transactional
    public void processOrder(Order order) {
        try {
            orderRepository.save(order);
            paymentService.processPayment(order);
        } catch (PaymentException e) {
            // 手动标记回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw e;
        }
    }
}
```

## 注解继承规则

### 基本继承行为

类级别的 `@Transactional` 注解会应用到该类及其子类的所有方法，但**不会向上影响祖先类**。

```java
// 祖先类
public class BaseService {
    public void baseMethod() { }     // 无事务
}

// 父类
public class ParentService extends BaseService {
    public void parentMethod() { }   // 无事务
}

// 子类
@Transactional  // 类级别注解
public class ChildService extends ParentService {
    
    public void childMethod1() { }    // 继承类级别事务
    
    @Transactional(readOnly = true)   // 方法级别覆盖类级别
    public void childMethod2() { }    // 使用只读事务
    
    // 重新声明继承的方法以获得事务支持
    @Override  
    public void parentMethod() {
        super.parentMethod();         // 现在有事务
    }
    
    // baseMethod() 仍然无事务（未重新声明）
}

// 后代类
public class GrandChildService extends ChildService {
    public void grandChildMethod() { }    // 继承父类的类级别事务
}
```

### 方法可见性限制

```java
@Transactional
public class Service {
    public void publicMethod() { }       // 事务生效
    protected void protectedMethod() { } // 事务生效  
    void packageMethod() { }             // 事务生效
    private void privateMethod() { }     // 事务不生效（Spring无法代理）
}
```

### 核心要点

1. **向下继承**：子类自动继承父类的类级别 `@Transactional`
2. **不向上影响**：子类的注解不会影响祖先类的方法
3. **方法覆盖**：方法级别注解优先于类级别
4. **重新声明**：要让继承的方法有事务，必须在子类中重新声明
5. **可见性限制**：只有 public/protected/package 方法的事务才生效

## 注解工作原理

`TransactionInterceptor`类的`invoke`方法是事务管理的核心。它会在方法执行前后进行拦截，创建事务上下文，根据方法上的`@Transactional`注解属性获取事务配置信息，如传播级别和异常信息。然后，它会检查是否已经存在一个事务上下文，根据传播级别决定是否需要创建新事务。如果需要，它会生成一个新的事务上下文对象`TransactionInfo`。接着，它会开启事务，获取数据库连接，关闭连接的自动提交，然后执行目标方法。如果方法执行过程中抛出异常，它会根据注解配置决定是否回滚事务。如果没有异常，它会提交事务。

`TransactionInterceptor`类核心代码：

```java
public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor, Serializable {

    // Transaction interceptor entry point
 public Object invoke(MethodInvocation invocation) throws Throwable {
  Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
  return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
 }

}
```

`TransactionAspectSupport`类核心代码：

```java
public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {

 protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
   final InvocationCallback invocation) throws Throwable {

        // If the transaction attribute is null, the method is non-transactional.
  TransactionAttributeSource tas = getTransactionAttributeSource();
        
        // Transaction attribute parsing
  final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);
        
        // Determine the specific transaction manager to use for the given transaction.
  final TransactionManager tm = determineTransactionManager(txAttr, targetClass);
        
  // Start transaction
  TransactionInfo txInfo = createTransactionIfNecessary(ptm, txAttr, joinpointIdentification);

  Object retVal;
  try {
   // Invoke business method
   retVal = invocation.proceedWithInvocation();
  } catch (Throwable ex) {
   // Rollback transaction
   completeTransactionAfterThrowing(txInfo, ex);
   throw ex;
  }
  // Commit transaction
  commitTransactionAfterReturning(txInfo);
  return retVal;
 }

}
```

## 注解生效原理

### 自动配置

Spring Boot通过 `TransactionAutoConfiguration` 自动启用事务管理，通常无需手动配置：

```java
@SpringBootApplication  // 已包含事务自动配置
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 手动配置

**场景1：自定义事务配置**

```java
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)  // 强制使用CGLIB代理
public class Application {
    // ...
}
```

**场景2：专门的配置类**

```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
```

### 代理模式

**JDK动态代理 vs CGLIB代理**

```java
// 配置代理模式
@EnableTransactionManagement(
    mode = AdviceMode.PROXY,           // 代理模式（默认）
    proxyTargetClass = true,           // 强制使用CGLIB
    order = Ordered.LOWEST_PRECEDENCE  // AOP顺序
)
public class TransactionConfig {
    // 配置
}
```

| 代理类型        | 适用场景       | 优缺点                        |
| --------------- | -------------- | ----------------------------- |
| **JDK动态代理** | 实现了接口的类 | 性能好，但需要接口            |
| **CGLIB代理**   | 没有接口的类   | 无需接口，但不能代理final方法 |

## 常见应用示例

### 服务层事务

```java
@Service
@Transactional  // 类级别默认配置
public class UserService {
    
    // 继承类级别事务配置
    public void createUser(User user) {
        userRepository.save(user);
        emailService.sendWelcomeEmail(user);
    }
    
    // 只读查询优化
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE);
    }
    
    // 独立事务处理
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserActivity(Long userId, String activity) {
        activityRepository.save(new UserActivity(userId, activity));
    }
}
```

### 多数据源事务

```java
@Configuration
public class TransactionConfig {
    
    @Bean
    @Primary
    public PlatformTransactionManager primaryTransactionManager() {
        return new DataSourceTransactionManager(primaryDataSource());
    }
    
    @Bean
    public PlatformTransactionManager secondaryTransactionManager() {
        return new DataSourceTransactionManager(secondaryDataSource());
    }
}

@Service
public class MultiDataSourceService {
    
    @Transactional  // 使用主事务管理器
    public void primaryOperation() {
        primaryRepository.save(entity);
    }
    
    @Transactional("secondaryTransactionManager")
    public void secondaryOperation() {
        secondaryRepository.save(entity);
    }
}
```

## 常见问题排查

### 内部调用不会触发事务代理

```java
@Service
public class ProblematicService {
    
    @Transactional
    public void outerMethod() {
        // ❌ 内部调用不会触发事务代理
        this.innerMethod();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void innerMethod() {
        // 不会创建新事务！
    }
    
    // ✅ 正确做法：通过其他Bean调用
    @Autowired
    private SelfService selfService;
    
    @Transactional
    public void correctOuterMethod() {
        selfService.innerMethod();  // 会正确创建新事务
    }
}
```

### 异步方法中事务不会正确传播

```java
@Service
public class AsyncService {
    
    @Transactional
    @Async  // ❌ 异步方法中事务不会正确传播
    public CompletableFuture<Void> asyncMethod() {
        // 事务在新线程中不可用
        return CompletableFuture.completedFuture(null);
    }
    
    // ✅ 正确做法：分离事务和异步
    @Async
    public CompletableFuture<Void> correctAsyncMethod() {
        return CompletableFuture.supplyAsync(() -> {
            transactionalService.doWork();
            return null;
        });
    }
}
```

# Spring编程式事务

```java
@Service
public class ProgrammaticTransactionService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    public void executeInTransaction() {
        transactionTemplate.execute(status -> {
            // 事务代码
            userRepository.save(user);
            
            if (someCondition) {
                status.setRollbackOnly();  // 标记回滚
            }
            
            return null;
        });
    }
    
    // 带返回值的事务
    public User createUserProgrammatically(User user) {
        return transactionTemplate.execute(status -> {
            User savedUser = userRepository.save(user);
            emailService.sendWelcomeEmail(savedUser);
            return savedUser;
        });
    }
}
```
