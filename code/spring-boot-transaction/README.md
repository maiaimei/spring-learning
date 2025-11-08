# Spring Boot 事务传播行为调试指南

## 7种事务传播行为说明

### 1. REQUIRED（默认）
- **行为**: 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
- **调试端点**: `GET /transaction/required`
- **断点位置**: `TransactionService.testRequired()` 和 `InnerTransactionService.innerRequired()`

### 2. SUPPORTS
- **行为**: 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行
- **调试端点**: `GET /transaction/supports`
- **断点位置**: `TransactionService.testSupports()` 和 `InnerTransactionService.innerSupports()`

### 3. MANDATORY
- **行为**: 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常
- **调试端点**: `GET /transaction/mandatory`
- **断点位置**: `TransactionService.testMandatory()` 和 `InnerTransactionService.innerMandatory()`

### 4. REQUIRES_NEW
- **行为**: 创建一个新的事务，如果当前存在事务，则把当前事务挂起
- **调试端点**: `GET /transaction/requires-new`
- **断点位置**: `TransactionService.testRequiresNew()` 和 `InnerTransactionService.innerRequiresNew()`

### 5. NOT_SUPPORTED
- **行为**: 以非事务方式执行操作，如果当前存在事务，则把当前事务挂起
- **调试端点**: `GET /transaction/not-supported`
- **断点位置**: `TransactionService.testNotSupported()` 和 `InnerTransactionService.innerNotSupported()`

### 6. NEVER
- **行为**: 以非事务方式执行，如果当前存在事务，则抛出异常
- **调试端点**: `GET /transaction/never`
- **断点位置**: `TransactionService.testNever()` 和 `InnerTransactionService.innerNever()`

### 7. NESTED
- **行为**: 如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行
- **调试端点**: `GET /transaction/nested`
- **断点位置**: `TransactionService.testNested()` 和 `InnerTransactionService.innerNested()`

## 调试步骤

### 基础调试
1. 启动Spring Boot应用
2. 在IDE中对应的方法上设置断点
3. 访问对应的HTTP端点
4. 在断点处查看事务状态

### 高级调试（推荐）
1. 使用 `TransactionDebugService.debugTransactionStatus()` 方法
2. 访问调试端点：
   - `GET /debug/required-nested` - 演示REQUIRED + NESTED组合
   - `GET /debug/required-new` - 演示REQUIRED + REQUIRES_NEW组合

### 关键调试点
在断点处可以查看以下变量来了解事务状态：
- `TransactionSynchronizationManager.isActualTransactionActive()` - 事务是否激活
- `TransactionSynchronizationManager.getCurrentTransactionName()` - 当前事务名称
- `TransactionSynchronizationManager.isCurrentTransactionReadOnly()` - 是否只读事务

## 注意事项
1. 确保方法调用是通过Spring代理进行的（使用@Autowired注入的bean调用）
2. 同一个类内部的方法调用不会触发事务代理，需要注入自身（self）来调用
3. 可以通过抛出异常来测试事务回滚行为