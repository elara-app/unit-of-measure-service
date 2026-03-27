package com.elara.app.unit_of_measure_service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public OpenAPI customOpenAPI() throws IOException {
        Map<String, Example> examples = loadExamples();
        
        return new OpenAPI()
            .info(new Info()
                .title("Unit of Measure Service API")
                .version("1.1.0")
                .description("RESTful API for managing Unit of Measure Status entities in the Elara platform.\n\n" +
                             "## Features\n" +
                             "- **CRUD Operations**: Create, read, update, and delete Unit of Measure Status records\n" +
                             "- **Pagination & Sorting**: Browse large datasets with configurable page size and sorting\n" +
                             "- **Search**: Find statuses by name using case-insensitive partial matching\n" +
                             "- **Filtering**: Filter statuses by usability status\n" +
                             "- **Validation**: Name uniqueness check and comprehensive input validation\n" +
                             "- **Status Management**: Dedicated endpoint for changing usability status with audit integrity\n\n" +
                             "## Base URL\n" +
                             "`/states/`\n\n" +
                             "## Response Format\n" +
                             "All responses use JSON format with consistent error handling and HTTP status codes.\n")
                .contact(new Contact()
                    .name("Elara Development Team")
                    .url("https://github.com/elara-app")
                    .email("dev@elara-app.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/license/mit")))
            .components(new Components()
                .addResponses("BadRequest", createErrorResponse("Bad Request - Invalid input data, validation errors, or missing required parameters"))
                .addResponses("NotFound", createErrorResponse("Not Found - The requested resource does not exist"))
                .addResponses("Conflict", createErrorResponse("Conflict - Resource already exists or operation conflicts with existing data"))
                .addResponses("InternalServerError", createErrorResponse("Internal Server Error - Unexpected error occurred on the server"))
                .addSchemas("ErrorResponse", createErrorResponseSchema())
                .addSchemas("UomStatusResponse", createUomStatusResponseSchema())
                .addSchemas("UomStatusRequest", createUomStatusRequestSchema())
                .addSchemas("UomStatusUpdate", createUomStatusUpdateSchema())
                .addSchemas("UomStatusPageResponse", createUomStatusPageResponseSchema())
                .addExamples("UomStatusCreated", examples.get("uom-status-created"))
                .addExamples("UomStatusUpdated", examples.get("uom-status-updated"))
                .addExamples("UomStatusPage", examples.get("uom-status-page"))
                .addExamples("ErrorBadRequest", examples.get("error-bad-request"))
                .addExamples("ErrorNotFound", examples.get("error-not-found"))
                .addExamples("ErrorConflict", examples.get("error-conflict"))
                .addExamples("ErrorDeleteConflict", examples.get("error-delete-conflict"))
                .addExamples("ErrorServer", examples.get("error-server")));
    }

    private Map<String, Example> loadExamples() throws IOException {
        Map<String, Example> examples = new HashMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        String[] exampleFiles = {
            "uom-status-created.json",
            "uom-status-updated.json",
            "uom-status-page.json",
            "error-bad-request.json",
            "error-not-found.json",
            "error-conflict.json",
            "error-delete-conflict.json",
            "error-server.json"
        };
        
        for (String fileName : exampleFiles) {
            Resource resource = resolver.getResource("classpath:examples/" + fileName);
            if (resource.exists()) {
                String content = new String(resource.getInputStream().readAllBytes());
                Map<String, Object> jsonMap = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
                Example example = new Example();
                example.setSummary(fileName.replace(".json", "").replace("-", " "));
                example.setValue(jsonMap);
                // Store with key without .json extension to match the retrieval below
                String key = fileName.replace(".json", "");
                examples.put(key, example);
            }
        }
        
        return examples;
    }

    private ApiResponse createErrorResponse(String description) {
        return new ApiResponse()
            .description(description)
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>()
                        .$ref("#/components/schemas/ErrorResponse"))));
    }

    private Schema<?> createErrorResponseSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.addProperties("code", new Schema<>().type("integer").description("Error code").example(1002));
        schema.addProperties("value", new Schema<>().type("string").description("Error code name").example("INVALID_DATA"));
        schema.addProperties("message", new Schema<>().type("string").description("Descriptive error message").example("Validation failed: Name must not be blank"));
        schema.addProperties("timestamp", new Schema<>().type("string").format("date-time").description("Error timestamp").example("2025-08-06T10:30:15"));
        schema.addProperties("path", new Schema<>().type("string").description("Request path").example("/states"));
        schema.addRequiredItem("code");
        schema.addRequiredItem("value");
        schema.addRequiredItem("message");
        schema.addRequiredItem("timestamp");
        schema.addRequiredItem("path");
        return schema;
    }

    private Schema<?> createUomStatusResponseSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.description("Unit of Measure Status response object");
        schema.addProperties("id", new Schema<>().type("integer").format("int64").description("Unique identifier").example(1));
        schema.addProperties("name", new Schema<>().type("string").description("Status name (unique)").example("Active"));
        schema.addProperties("description", new Schema<>().type("string").description("Status description (max 200 chars)").example("Unit of measure is currently active and can be used in transactions"));
        schema.addProperties("isUsable", new Schema<>().type("boolean").description("Whether this status is usable").example(true));
        schema.addRequiredItem("id");
        schema.addRequiredItem("name");
        schema.addRequiredItem("isUsable");
        return schema;
    }

    private Schema<?> createUomStatusRequestSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.description("Unit of Measure Status creation request");
        schema.addProperty("name", new Schema<>().type("string").description("Status name (1-50 chars, required)").example("Active"));
        schema.addProperty("description", new Schema<>().type("string").description("Status description (max 200 chars, optional)").example("Unit of measure is currently active and can be used in transactions"));
        schema.addProperty("isUsable", new Schema<>().type("boolean").description("Whether this status is usable (required)").example(true));
        schema.addRequiredItem("name");
        schema.addRequiredItem("isUsable");
        return schema;
    }

    private Schema<?> createUomStatusUpdateSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.description("Unit of Measure Status update request. Note: isUsable cannot be changed here, use the change-usability endpoint instead");
        schema.addProperty("name", new Schema<>().type("string").description("New status name (1-50 chars, optional)").example("Active"));
        schema.addProperty("description", new Schema<>().type("string").description("New status description (max 200 chars, optional)").example("Unit of measure is currently active and can be used in transactions"));
        return schema;
    }

    private Schema<?> createUomStatusPageResponseSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.description("Paginated response for Unit of Measure Status resources");
        schema.addProperties("content", new Schema<>()
            .type("array")
            .items(new Schema<>().$ref("#/components/schemas/UomStatusResponse"))
            .description("Page content"));
        schema.addProperties("totalElements", new Schema<>().type("integer").format("int64").example(3));
        schema.addProperties("totalPages", new Schema<>().type("integer").example(1));
        schema.addProperties("number", new Schema<>().type("integer").example(0));
        schema.addProperties("size", new Schema<>().type("integer").example(20));
        schema.addProperties("numberOfElements", new Schema<>().type("integer").example(3));
        schema.addProperties("first", new Schema<>().type("boolean").example(true));
        schema.addProperties("last", new Schema<>().type("boolean").example(true));
        schema.addProperties("empty", new Schema<>().type("boolean").example(false));
        return schema;
    }
}
