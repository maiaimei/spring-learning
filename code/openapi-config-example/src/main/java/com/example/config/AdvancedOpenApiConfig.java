package com.example.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdvancedOpenApiConfig {

  //@Bean
  public OpenApiCustomizer globalResponseCustomizer() {
    return openApi -> {
      Components components = openApi.getComponents();
      if (components == null) {
        components = new Components();
        openApi.setComponents(components);
      }

      // 定义通用响应模式
      Schema<?> resultSchema = new Schema<>()
          .type("object")
          .addProperty("code", new Schema<>().type("integer").description("响应码"))
          .addProperty("message", new Schema<>().type("string").description("响应消息"))
          .addProperty("data", new Schema<>().description("响应数据"));

      Schema<?> errorSchema = new Schema<>()
          .type("object")
          .addProperty("code", new Schema<>().type("integer").description("错误码"))
          .addProperty("message", new Schema<>().type("string").description("错误消息"))
          .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("时间戳"));

      components.addSchemas("Result", resultSchema);
      components.addSchemas("ErrorResponse", errorSchema);

      // 定义全局响应
      ApiResponse badRequestResponse = new ApiResponse()
          .description("请求参数错误")
          .content(new Content().addMediaType("application/json",
              new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

      ApiResponse serverErrorResponse = new ApiResponse()
          .description("服务器内部错误")
          .content(new Content().addMediaType("application/json",
              new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

      components.addResponses("BadRequest", badRequestResponse);
      components.addResponses("ServerError", serverErrorResponse);

      // 为所有路径添加全局响应
      if (openApi.getPaths() != null) {
        openApi.getPaths().values().forEach(pathItem ->
            pathItem.readOperations().forEach(operation -> {
              if (operation.getResponses() != null) {
                operation.getResponses().addApiResponse("400", new ApiResponse().$ref("#/components/responses/BadRequest"));
                operation.getResponses().addApiResponse("500", new ApiResponse().$ref("#/components/responses/ServerError"));
              }
            })
        );
      }
    };
  }

  @Bean
  public OpenApiCustomizer advancedGlobalResponseCustomizer() {
    return openApi -> {
      // 确保Components存在
      Components components = openApi.getComponents();
      if (components == null) {
        components = new Components();
        openApi.setComponents(components);
      }

      // 添加全局响应Schema
      addGlobalSchemas(components);

      // 添加全局响应定义
      addGlobalResponses(components);

      // 为所有操作添加全局响应
      addGlobalResponsesToOperations(openApi);
    };
  }

  private void addGlobalSchemas(Components components) {
    // 成功响应Schema
    Schema<?> successResultSchema = new Schema<>()
        .type("object")
        .addProperty("code", new Schema<>().type("integer").example(200).description("响应码"))
        .addProperty("message", new Schema<>().type("string").example("操作成功").description("响应消息"))
        .addProperty("data", new Schema<>().description("响应数据"))
        .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("响应时间"));

    // 错误响应Schema
    Schema<?> errorResponseSchema = new Schema<>()
        .type("object")
        .addProperty("code", new Schema<>().type("integer").example(400).description("错误码"))
        .addProperty("message", new Schema<>().type("string").example("请求参数错误").description("错误消息"))
        .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("错误时间"))
        .addProperty("path", new Schema<>().type("string").description("请求路径"));

    // 分页响应Schema
    Schema<?> pageResultSchema = new Schema<>()
        .type("object")
        .addProperty("code", new Schema<>().type("integer").example(200))
        .addProperty("message", new Schema<>().type("string").example("查询成功"))
        .addProperty("data", new Schema<>()
            .type("object")
            .addProperty("content", new Schema<>().type("array").items(new Schema<>()))
            .addProperty("totalElements", new Schema<>().type("integer"))
            .addProperty("totalPages", new Schema<>().type("integer"))
            .addProperty("size", new Schema<>().type("integer"))
            .addProperty("number", new Schema<>().type("integer")));

    components.addSchemas("SuccessResult", successResultSchema);
    components.addSchemas("ErrorResponse", errorResponseSchema);
    components.addSchemas("PageResult", pageResultSchema);
  }

  private void addGlobalResponses(Components components) {
    // 400 错误响应
    ApiResponse badRequestResponse = new ApiResponse()
        .description("请求参数错误")
        .content(new Content().addMediaType("application/json",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

    // 401 未授权响应
    ApiResponse unauthorizedResponse = new ApiResponse()
        .description("未授权访问")
        .content(new Content().addMediaType("application/json",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

    // 403 禁止访问响应
    ApiResponse forbiddenResponse = new ApiResponse()
        .description("禁止访问")
        .content(new Content().addMediaType("application/json",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

    // 404 未找到响应
    ApiResponse notFoundResponse = new ApiResponse()
        .description("资源未找到")
        .content(new Content().addMediaType("application/json",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

    // 500 服务器错误响应
    ApiResponse serverErrorResponse = new ApiResponse()
        .description("服务器内部错误")
        .content(new Content().addMediaType("application/json",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));

    components.addResponses("BadRequest", badRequestResponse);
    components.addResponses("Unauthorized", unauthorizedResponse);
    components.addResponses("Forbidden", forbiddenResponse);
    components.addResponses("NotFound", notFoundResponse);
    components.addResponses("ServerError", serverErrorResponse);
  }

  private void addGlobalResponsesToOperations(OpenAPI openApi) {
    if (openApi.getPaths() != null) {
      openApi.getPaths().values().forEach(pathItem ->
          pathItem.readOperations().forEach(this::addGlobalResponsesToOperation)
      );
    }
  }

  private void addGlobalResponsesToOperation(Operation operation) {
    ApiResponses responses = operation.getResponses();
    if (responses == null) {
      responses = new ApiResponses();
      operation.setResponses(responses);
    }

    // 只添加不存在的响应码
    Map<String, ApiResponse> responseMap = responses;

    if (!responseMap.containsKey("400")) {
      responses.addApiResponse("400", new ApiResponse().$ref("#/components/responses/BadRequest"));
    }
    if (!responseMap.containsKey("401")) {
      responses.addApiResponse("401", new ApiResponse().$ref("#/components/responses/Unauthorized"));
    }
    if (!responseMap.containsKey("403")) {
      responses.addApiResponse("403", new ApiResponse().$ref("#/components/responses/Forbidden"));
    }
    if (!responseMap.containsKey("404")) {
      responses.addApiResponse("404", new ApiResponse().$ref("#/components/responses/NotFound"));
    }
    if (!responseMap.containsKey("500")) {
      responses.addApiResponse("500", new ApiResponse().$ref("#/components/responses/ServerError"));
    }
  }
}