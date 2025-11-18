# SpringDoc OpenAPI 全局响应配置示例

使用 `springdoc-openapi-starter-webmvc-ui:2.8.4` 配置全局响应处理和异常处理的完整示例。

## 快速开始

```bash
mvn spring-boot:run
```

访问 API 文档：<http://localhost:8080/swagger-ui/index.html>
访问 ：<http://localhost:8080/v3/api-docs>

## 核心功能

### OpenApiCustomizer 配置

- **OpenApiConfig**: 基础全局响应配置
- **AdvancedOpenApiConfig**: 完整的全局响应处理（400/401/403/404/500）

### 全局异常处理

- **GlobalExceptionHandler**: 统一异常处理，返回标准错误格式
- 使用 `@Hidden` 隐藏异常处理器，不在API文档中显示

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00"
}
```

## 测试接口

- `GET /api/users/{id}` - 获取用户信息
- `POST /api/users` - 创建用户

## 配置说明

所有API接口自动包含以下全局响应：

- 400: 请求参数错误
- 401: 未授权访问  
- 403: 禁止访问
- 404: 资源未找到
- 500: 服务器内部错误

```
// springdoc配置对应的类
org.springdoc.core.properties.SpringDocConfigProperties

// swagger-ui配置对应的类  
org.springdoc.core.properties.SwaggerUiConfigProperties

// api-docs配置对应的类
org.springdoc.core.properties.SpringDocConfigProperties
```
