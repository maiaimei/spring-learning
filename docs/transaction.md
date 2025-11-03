

## 概述

**`@Transactional`** 是Spring框架提供的声明式事务管理注解，用于自动将方法或类包装在数据库事务中。

### 注解定义

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

### 依赖配置

`@Transactional` 注解位于 **`spring-tx`** 模块中。

Maven依赖：
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-tx</artifactId>
</dependency>
```

**注意**：使用Spring Boot时，该依赖通常通过 `spring-boot-starter-jdbc`、`spring-boot-starter-data-jpa` 等starter自动包含。

## Spring Boot中的事务配置

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

### 代理模式选择

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

### 验证事务是否启用

```java
@Component
public class TransactionChecker {
    
    @Autowired
    private ApplicationContext context;
    
    @PostConstruct
    public void checkTransaction() {
        boolean hasTransactionManager = context.containsBean("transactionManager");
        System.out.println("Transaction enabled: " + hasTransactionManager);
    }
}
```

## 注解的继承规则

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

### 只读事务

```java
@Transactional(readOnly = true)
public User findUser(Long id) {
    // 只读事务，优化性能，防止意外修改
    return userRepository.findById(id);
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

### 事务传播行为

事务传播行为核心在于控制事务的边界和多个事务方法之间的协作关系。例如，当方法A（已开启事务）调用方法B（也需要事务管理）时，方法B是继续使用A的事务，还是自己新开一个事务，或是其他处理方式。

#### Propagation 枚举值

|  | 传播行为 | 说明 | 使用场景 |
|---------|------|----------|----------|
| 1 | **REQUIRED** | Support a current transaction, create a new one if none exists. This is the default setting of a transaction annotation.<br />默认值。如果当前没有事务，就新建一个事务；如果已经存在事务，则加入该事务。 | 大多数业务方法 |
| 2 | **REQUIRES_NEW** | Create a new transaction, and suspend the current transaction if one exists. <br />总是新建事务，如果当前存在事务，就把当前事务挂起。 | 独立的日志记录、审计 |
| 3 | **SUPPORTS** | Support a current transaction, execute non-transactionally if none exists. <br />支持当前事务，如果当前没有事务，就以非事务方式执行。 | 查询方法 |
| 4 | **NOT_SUPPORTED** | Execute non-transactionally, suspend the current transaction if one exists. <br />以非事务方式执行，如果当前存在事务，就把当前事务挂起。 | 不需要事务的操作 |
| 5 | **MANDATORY** | Support a current transaction, throw an exception if none exists. <br />使用当前的事务，如果当前没有事务，就抛出异常。 | 强制事务上下文的方法 |
| 6 | **NEVER** | Execute non-transactionally, throw an exception if a transaction exists. <br />以非事务方式执行，如果当前存在事务，则抛出异常。 | 绝对不能在事务中的操作 |
| 7 | **NESTED** | Execute within a nested transaction if a current transaction exists, behave like REQUIRED otherwise.<br />如果当前存在事务，则在嵌套事务内执行；如果当前没有事务，等同于REQUIRED | 部分回滚场景 |

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

#### Isolation 枚举值

| 隔离级别 | 说明 | 解决的问题 | 可能出现的问题 |
|---------|------|------------|----------------|
| **DEFAULT** | 使用数据库默认隔离级别 | - | 取决于数据库 |
| **READ_UNCOMMITTED** | 读未提交 | - | 脏读、不可重复读、幻读 |
| **READ_COMMITTED** | 读已提交 | 脏读 | 不可重复读、幻读 |
| **REPEATABLE_READ** | 可重复读 | 脏读、不可重复读 | 幻读 |
| **SERIALIZABLE** | 串行化 | 脏读、不可重复读、幻读 | 性能最低 |

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

## 声明式事务的实现原理

### 核心逻辑

Spring框架声明式事务支持的核心要点：

- **AOP代理实现**：通过[AOP代理](https://docs.spring.io/spring-framework/reference/core/aop/proxying.html#aop-understanding-aop-proxies)启用事务支持
- **元数据驱动**：事务通知由元数据（XML或注解）驱动
- **拦截器协作**：AOP代理使用 `TransactionInterceptor` 与 `TransactionManager` 协同工作


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

`AbstractPlatformTransactionManager`类核心代码

```java
public abstract class AbstractPlatformTransactionManager
		implements PlatformTransactionManager, ConfigurableTransactionManager, Serializable {
    
    public final TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
			throws TransactionException {
    }
    
    public final void commit(TransactionStatus status) throws TransactionException { 
    }
    
    public final void rollback(TransactionStatus status) throws TransactionException {
    }

}
```

`DataSourceTransactionManager`类核心代码：

```java
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager
		implements ResourceTransactionManager, InitializingBean {
    
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        Connection con = null;
        con = txObject.getConnectionHolder().getConnection();
        con.setAutoCommit(false);
    }
    
    protected void doCommit(DefaultTransactionStatus status) {
		Connection con = txObject.getConnectionHolder().getConnection();
		con.commit();
	}
    
    protected void doRollback(DefaultTransactionStatus status) {
        Connection con = txObject.getConnectionHolder().getConnection();
        con.rollback();
    }

}
```

### 事务拦截器

`TransactionInterceptor` 支持两种编程模型：

**命令式事务管理**
- 适用于返回类型为 `void` 及其他常规类型的方法
- 使用 `PlatformTransactionManager`
- 基于线程绑定事务，在当前执行线程内暴露事务
- **注意**：不会传播到方法内新启动的线程

**响应式事务管理**
- 适用于返回 `Publisher`、Kotlin `Flow` 等响应式类型的方法
- 使用 `ReactiveTransactionManager`
- 基于 Reactor 上下文而非线程本地属性
- 所有参与的数据访问操作需在同一响应式管道的相同 Reactor 上下文中执行

### 事务代理调用流程

![事务代理调用概念图](./images/tx.png)

## 常见使用模式

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

## spring-tx 模块

**`spring-tx`** JAR 提供Spring事务管理的核心功能：

### 主要功能

- **声明式事务管理** - 使用 `@Transactional` 注解
- **编程式事务管理** - 通过 `TransactionTemplate` 手动控制事务
- **事务抽象层** - 为不同事务管理器（JPA、JDBC、JMS等）提供统一API
- **事务传播机制** - 控制方法调用时事务的行为
- **事务隔离级别** - 支持多种数据库隔离级别
- **回滚规则** - 灵活的异常回滚配置

### 编程式事务示例

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
