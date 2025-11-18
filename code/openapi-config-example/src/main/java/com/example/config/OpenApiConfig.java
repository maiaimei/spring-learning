package com.example.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
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
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new io.swagger.v3.oas.models.info.Info()
            .title("API文档")
            .version("1.0")
            .description("Spring Boot OpenAPI示例")
            .contact(new Contact()
                .name("Your Name")
                .email("your.email@example.com")));
  }
}