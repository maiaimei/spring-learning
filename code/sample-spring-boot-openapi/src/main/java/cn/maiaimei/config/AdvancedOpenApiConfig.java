package cn.maiaimei.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    components.addSchemas("BadRequestResponse", createBadRequestResponseSchema());
    components.addSchemas("UnauthorizedResponse", createErrorResponseSchema(HttpStatus.UNAUTHORIZED));
    components.addSchemas("ForbiddenResponse", createErrorResponseSchema(HttpStatus.FORBIDDEN));
    components.addSchemas("NotFoundResponse", createErrorResponseSchema(HttpStatus.NOT_FOUND));
    components.addSchemas("InternalServerErrorResponse", createErrorResponseSchema(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  private Schema<?> createSuccessResponseSchema(Schema<?> dataSchema) {
    HttpStatus httpStatus = HttpStatus.OK;
    return new ObjectSchema()
        .description(httpStatus.getReasonPhrase())
        .addProperty("code", new IntegerSchema().description("Response code"))
        .addProperty("message", new StringSchema().description("Response message"))
        .addProperty("data", dataSchema)
        .addProperty("timestamp", new StringSchema().format("date-time").description("Response timestamp"));
  }

  private Schema<?> createErrorResponseSchema(HttpStatus httpStatus) {
    return new ObjectSchema()
        .description(httpStatus.getReasonPhrase())
        .addProperty("code", new IntegerSchema().description("Error code").example(httpStatus.value()))
        .addProperty("message", new StringSchema().description("Error message").example(httpStatus.getReasonPhrase()))
        .addProperty("path", new StringSchema().description("Request path").example("/path/to"))
        .addProperty("timestamp",
            new DateTimeSchema().description("Error timestamp").example("2025-12-01T14:44:22.892Z"));
  }

  private Schema<?> createBadRequestResponseSchema() {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    return new ObjectSchema()
        .description(httpStatus.getReasonPhrase())
        .addProperty("code", new IntegerSchema().description("Error code").example(httpStatus.value()))
        .addProperty("message", new StringSchema().description("Error message").example(httpStatus.getReasonPhrase()))
        .addProperty("errors", new ArraySchema().items(new ObjectSchema()
                .addProperty("field", new StringSchema().description("Error field"))
                .addProperty("message", new StringSchema().description("Field error message"))
                .addProperty("rejectedValue", new ObjectSchema().description("Rejected value")))
            .description("Detailed error information list"))
        .addProperty("path", new StringSchema().description("Request path").example("/path/to"))
        .addProperty("timestamp",
            new DateTimeSchema().description("Error timestamp").example("2025-12-01T14:44:22.892Z"));
  }

  private void addGlobalResponses(Components components) {
    components.addResponses("BadRequest",
        createErrorResponse(components, "BadRequestResponse", HttpStatus.BAD_REQUEST));
    components.addResponses("Unauthorized",
        createErrorResponse(components, "UnauthorizedResponse", HttpStatus.UNAUTHORIZED));
    components.addResponses("Forbidden", createErrorResponse(components, "ForbiddenResponse", HttpStatus.FORBIDDEN));
    components.addResponses("NotFound", createErrorResponse(components, "NotFoundResponse", HttpStatus.NOT_FOUND));
    components.addResponses("InternalServerError",
        createErrorResponse(components, "InternalServerErrorResponse", HttpStatus.INTERNAL_SERVER_ERROR));
  }

  private ApiResponse createErrorResponse(Components components, String schemaName, HttpStatus httpStatus) {
    Map<String, Object> example = new LinkedHashMap<>();
    example.put("code", httpStatus.value());
    example.put("message", httpStatus.getReasonPhrase());
    example.put("path", "/path/to");
    example.put("timestamp", "2025-12-01T14:44:22.892Z");
    final Schema<?> schema = components.getSchemas().get(schemaName);
    return new ApiResponse()
        .description(schema.getDescription())
        .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
            new MediaType().schema(schema).example(example)));
  }

  private void addGlobalResponsesToOperations(OpenAPI openApi) {
    if (!CollectionUtils.isEmpty(openApi.getPaths())) {
      openApi.getPaths().values()
          .forEach(pathItem -> pathItem.readOperations().forEach(this::addGlobalResponsesToOperation));
    }
  }

  private void addGlobalResponsesToOperation(Operation operation) {
    ApiResponses responses = operation.getResponses();
    if (Objects.isNull(responses)) {
      responses = new ApiResponses();
      operation.setResponses(responses);
    }

    // Check if operation has SkipWrapResponse annotation
    boolean skipWrap = hasSkipWrapResponseAnnotation(operation);

    // Wrap existing 200 responses with unified response structure
    if (!skipWrap) {
      wrapSuccessResponses(responses);
    }

    // Add global error responses
    addErrorResponses(responses);
  }

  private boolean hasSkipWrapResponseAnnotation(Operation operation) {
    return Objects.nonNull(operation.getExtensions()) && operation.getExtensions().containsKey("x-skip-wrap-response");
  }

  private void wrapSuccessResponses(ApiResponses responses) {
    ApiResponse originalResponse = responses.get("200");

    if (originalResponse != null && originalResponse.getContent() != null) {
      // Extract original response schema
      Content originalContent = originalResponse.getContent();
      MediaType originalMediaType = originalContent.get("application/json");

      if (originalMediaType != null && originalMediaType.getSchema() != null) {
        // Create wrapped response with original data as 'data' property
        final Schema<?> originalSchema = originalMediaType.getSchema();
        Schema<?> wrappedSchema = createSuccessResponseSchema(originalSchema);
        Map<String, Object> wrappedExample = buildWrappedExample(originalSchema);

        // Replace original response with wrapped version
        ApiResponse wrappedResponse = new ApiResponse()
            .description(Objects.toString(originalResponse.getDescription(), HttpStatus.OK.getReasonPhrase()))
            .content(new Content().addMediaType("application/json",
                new MediaType().schema(wrappedSchema).example(wrappedExample)));

        responses.addApiResponse("200", wrappedResponse);
      }
    } else {
      // No existing 200 response, add default wrapped response
      responses.addApiResponse("200", new ApiResponse()
          .description(HttpStatus.OK.getReasonPhrase())
          .content(new Content().addMediaType("application/json",
              new MediaType().schema(createSuccessResponseSchema(null)))));
    }
  }

  private Map<String, Object> buildWrappedExample(Schema<?> originalSchema) {
    Map<String, Object> wrappedExample = new LinkedHashMap<>();
    wrappedExample.put("code", 200);
    wrappedExample.put("message", "Operation successful");
    wrappedExample.put("data", buildExampleFromSchema(originalSchema));
    wrappedExample.put("timestamp", "2025-12-01T14:44:22.892Z");
    return wrappedExample;
  }

  private Object buildExampleFromSchema(Schema<?> schema) {
    if (schema == null) {
      return null;
    }

    if (schema.getExample() != null) {
      return schema.getExample();
    }

    if (StringUtils.hasText(schema.get$ref())) {

    }

    String type = schema.getType();
    if ("array".equals(type)) {
      return List.of(buildExampleFromSchema(schema.getItems()));
    }

    if ("object".equals(type) && schema.getProperties() != null) {
      Map<String, Object> example = new LinkedHashMap<>();
      schema.getProperties().forEach((key, propertySchema) -> example.put(key, buildExampleFromSchema(propertySchema)));
      return example;
    }

    return getDefaultValueByType(type);
  }

  private Object getDefaultValueByType(String type) {
    return switch (type) {
      case "string" -> "string";
      case "integer" -> 0;
      case "number" -> 0.0;
      case "boolean" -> false;
      default -> null;
    };
  }

  private void addErrorResponses(ApiResponses responses) {
    if (!responses.containsKey("400")) {
      responses.addApiResponse("400", new ApiResponse().$ref("#/components/responses/BadRequest"));
    }
    if (!responses.containsKey("401")) {
      responses.addApiResponse("401", new ApiResponse().$ref("#/components/responses/Unauthorized"));
    }
    if (!responses.containsKey("403")) {
      responses.addApiResponse("403", new ApiResponse().$ref("#/components/responses/Forbidden"));
    }
    if (!responses.containsKey("404")) {
      responses.addApiResponse("404", new ApiResponse().$ref("#/components/responses/NotFound"));
    }
    if (!responses.containsKey("500")) {
      responses.addApiResponse("500", new ApiResponse().$ref("#/components/responses/InternalServerError"));
    }
  }
}