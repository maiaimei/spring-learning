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
import java.util.Objects;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdvancedOpenApiConfig {

  @Bean
  public OpenApiCustomizer advancedGlobalResponseCustomizer() {
    return openApi -> {
      // Ensure components exist
      Components components = openApi.getComponents();
      if (Objects.isNull(components)) {
        components = new Components();
        openApi.setComponents(components);
      }

      // Add global response schemas
      addGlobalSchemas(components);

      // Add global response definitions
      addGlobalResponses(components);

      // Add global responses to all operations
      addGlobalResponsesToOperations(openApi);
    };
  }

  private void addGlobalSchemas(Components components) {
    // Add success response schema
    Schema<?> successResponseSchema = new Schema<>()
        .type("object")
        .addProperty("code", new Schema<>().type("integer").example(200).description("Response code"))
        .addProperty("message", new Schema<>().type("string").example("Operation successful").description("Response message"))
        .addProperty("data", new Schema<>().description("Response data"))
        .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("Response timestamp"));
    components.addSchemas("SuccessResponse", successResponseSchema);

    // Add error response schemas
    components.addSchemas("BadRequestResponse", newBadRequestResponseSchema());
    components.addSchemas("UnauthorizedResponse", newErrorResponseSchema(401, "Unauthorized", "Unauthorized access"));
    components.addSchemas("ForbiddenResponse", newErrorResponseSchema(403, "Forbidden", "Access forbidden"));
    components.addSchemas("NotFoundResponse", newErrorResponseSchema(404, "Not found", "Resource not found"));
    components.addSchemas("ServerErrorResponse", newErrorResponseSchema(500, "Internal server error", "Internal server error"));
  }

  private Schema<?> newBadRequestResponseSchema() {
    final Schema<?> schema = newErrorResponseSchema(400, "Bad request", "Bad request");
    return schema.addProperty("errors", new Schema<>().type("array")
        .items(new Schema<>().type("object")
            .addProperty("field", new Schema<>().type("string").description("Error field"))
            .addProperty("message", new Schema<>().type("string").description("Field error message"))
            .addProperty("rejectedValue", new Schema<>().description("Rejected value")))
        .description("Detailed error information list"));
  }

  private Schema<?> newErrorResponseSchema(int code, String description, String message) {
    return new Schema<>()
        .type("object")
        .description(description)
        .addProperty("code", new Schema<>().type("integer").example(code).description("Error code"))
        .addProperty("message", new Schema<>().type("string").example(message).description("Error message"))
        .addProperty("path", new Schema<>().type("string").example("/api/example").description("Request path"))
        .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("Error timestamp"));
  }

  private void addGlobalResponses(Components components) {
    // 400 Bad Request response
    ApiResponse badRequestResponse = new ApiResponse()
        .description("Bad request")
        .content(new Content().addMediaType("application/json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/BadRequestResponse"))));

    // 401 Unauthorized response
    ApiResponse unauthorizedResponse = new ApiResponse()
        .description("Unauthorized")
        .content(new Content().addMediaType("application/json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/UnauthorizedResponse"))));

    // 403 Forbidden response
    ApiResponse forbiddenResponse = new ApiResponse()
        .description("Forbidden")
        .content(new Content().addMediaType("application/json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/ForbiddenResponse"))));

    // 404 Not Found response
    ApiResponse notFoundResponse = new ApiResponse()
        .description("Not found")
        .content(new Content().addMediaType("application/json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/NotFoundResponse"))));

    // 500 Internal Server Error response
    ApiResponse serverErrorResponse = new ApiResponse()
        .description("Internal server error")
        .content(new Content().addMediaType("application/json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/ServerErrorResponse"))));

    components.addResponses("BadRequest", badRequestResponse);
    components.addResponses("Unauthorized", unauthorizedResponse);
    components.addResponses("Forbidden", forbiddenResponse);
    components.addResponses("NotFound", notFoundResponse);
    components.addResponses("ServerError", serverErrorResponse);
  }

  private void addGlobalResponsesToOperations(OpenAPI openApi) {
    if (openApi.getPaths() != null) {
      openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(this::addGlobalResponsesToOperation));
    }
  }

  private void addGlobalResponsesToOperation(Operation operation) {
    ApiResponses responses = operation.getResponses();
    if (responses == null) {
      responses = new ApiResponses();
      operation.setResponses(responses);
    }

    // Wrap existing 200 responses with unified response structure
    wrapSuccessResponses(responses);

    // Add global error responses
    addErrorResponses(responses);
  }

  private void wrapSuccessResponses(ApiResponses responses) {
    ApiResponse originalResponse = responses.get("200");
    
    if (originalResponse != null && originalResponse.getContent() != null) {
      // Extract original response schema
      Content originalContent = originalResponse.getContent();
      MediaType originalMediaType = originalContent.get("application/json");
      
      if (originalMediaType != null && originalMediaType.getSchema() != null) {
        // Create wrapped response with original data as 'data' property
        Schema<?> wrappedSchema = new Schema<>()
            .type("object")
            .addProperty("code", new Schema<>().type("integer").example(200).description("Response code"))
            .addProperty("message", new Schema<>().type("string").example("Operation successful").description("Response message"))
            .addProperty("data", originalMediaType.getSchema())
            .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("Response timestamp"));
        
        // Replace original response with wrapped version
        ApiResponse wrappedResponse = new ApiResponse()
            .description(originalResponse.getDescription() != null ? originalResponse.getDescription() : "Operation successful")
            .content(new Content().addMediaType("application/json", new MediaType().schema(wrappedSchema)));
        
        responses.addApiResponse("200", wrappedResponse);
      }
    } else {
      // No existing 200 response, add default wrapped response
      responses.addApiResponse("200", new ApiResponse()
          .description("Operation successful")
          .content(new Content().addMediaType("application/json", 
              new MediaType().schema(new Schema<>().$ref("#/components/schemas/SuccessResponse")))));
    }
  }

  private void addErrorResponses(ApiResponses responses) {
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