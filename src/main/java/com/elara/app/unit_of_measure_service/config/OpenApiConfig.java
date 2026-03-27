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
import io.swagger.v3.oas.models.media.ArraySchema;
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
                .description("""
                        RESTful API for managing Unit of Measure and Unit of Measure Status entities in the Elara platform.
                        
                        ## Features
                        - **UOM Operations**: Create, read, update, delete, search, and filter Units of Measure
                        - **CRUD Operations**: Create, read, update, and delete Unit of Measure Status records
                        - **Pagination & Sorting**: Browse large datasets with configurable page size and sorting
                        - **Search**: Find statuses by name using case-insensitive partial matching
                        - **Filtering**: Filter statuses by usability status
                        - **Validation**: Name uniqueness check and comprehensive input validation
                        - **Status Management**: Dedicated endpoints for changing association/usable state with audit integrity
                        
                        ## Resource Paths
                        - `/**` for Unit of Measure operations
                        - `/states/**` for Unit of Measure Status operations
                        
                        ## Response Format
                        All responses use JSON format with consistent error handling and HTTP status codes.
                        """)
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
                .addSchemas("UomResponse", createUomResponseSchema())
                .addSchemas("UomRequest", createUomRequestSchema())
                .addSchemas("UomUpdate", createUomUpdateSchema())
                .addSchemas("UomPageResponse", createUomPageResponseSchema())
                .addExamples("UomCreated", examples.get("uom-created"))
                .addExamples("UomUpdated", examples.get("uom-updated"))
                .addExamples("UomPage", examples.get("uom-page"))
                .addExamples("UomStatusCreated", examples.get("uom-status-created"))
                .addExamples("UomStatusUpdated", examples.get("uom-status-updated"))
                .addExamples("UomStatusPage", examples.get("uom-status-page"))
                .addExamples("ErrorBadRequestUom", examples.get("error-bad-request-uom"))
                .addExamples("ErrorUomNotFound", examples.get("error-uom-not-found"))
                .addExamples("ErrorUomStatusNotFound", examples.get("error-uom-status-not-found"))
                .addExamples("ErrorUomConflict", examples.get("error-uom-conflict"))
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
            "uom-created.json",
            "uom-updated.json",
            "uom-page.json",
            "uom-status-created.json",
            "uom-status-updated.json",
            "uom-status-page.json",
            "error-bad-request-uom.json",
            "error-uom-not-found.json",
            "error-uom-status-not-found.json",
            "error-uom-conflict.json",
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
                Map<String, Object> jsonMap = objectMapper.readValue(content, new TypeReference<>() {});
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
        schema.addProperty("code", new Schema<>().type("integer").description("Error code").example(1002));
        schema.addProperty("value", new Schema<>().type("string").description("Error code name").example("INVALID_DATA"));
        schema.addProperty("message", new Schema<>().type("string").description("Descriptive error message").example("Validation failed: Name must not be blank"));
        schema.addProperty("timestamp", new Schema<>().type("string").format("date-time").description("Error timestamp").example("2025-08-06T10:30:15"));
        schema.addProperty("path", new Schema<>().type("string").description("Request path").example("/states"));
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
        schema.addProperty("id", new Schema<>().type("integer").format("int64").description("Unique identifier").example(1));
        schema.addProperty("name", new Schema<>().type("string").description("Status name (unique)").example("Active"));
        schema.addProperty("description", new Schema<>().type("string").description("Status description (max 200 chars)").example("Unit of measure is currently active and can be used in transactions"));
        schema.addProperty("isUsable", new Schema<>().type("boolean").description("Whether this status is usable").example(true));
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
        schema.addProperty("content", new ArraySchema()
            .items(new Schema<>().$ref("#/components/schemas/UomStatusResponse"))
            .description("Page content"));
        schema.addProperty("totalElements", new Schema<>().type("integer").format("int64").example(3));
        schema.addProperty("totalPages", new Schema<>().type("integer").example(1));
        schema.addProperty("number", new Schema<>().type("integer").example(0));
        schema.addProperty("size", new Schema<>().type("integer").example(20));
        schema.addProperty("numberOfElements", new Schema<>().type("integer").example(3));
        schema.addProperty("first", new Schema<>().type("boolean").example(true));
        schema.addProperty("last", new Schema<>().type("boolean").example(true));
        schema.addProperty("empty", new Schema<>().type("boolean").example(false));
        return schema;
    }

    private Schema<?> createUomResponseSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.description("Unit of Measure response object");
        schema.addProperty("id", new Schema<>().type("integer").format("int64").description("Unique identifier").example(1));
        schema.addProperty("name", new Schema<>().type("string").description("Unit name (unique)").example("Kilogram"));
        schema.addProperty("description", new Schema<>().type("string").description("Unit description (max 200 chars)").example("Base unit of mass in SI"));
        schema.addProperty("conversionFactorToBase", new Schema<>().type("number").description("Positive factor relative to the base unit").example(1.000));
        schema.addProperty("uomStatusId", new Schema<>().type("integer").format("int64").description("Associated UOM status identifier").example(1));
        schema.addRequiredItem("id");
        schema.addRequiredItem("name");
        schema.addRequiredItem("conversionFactorToBase");
        return schema;
    }

    private Schema<?> createUomRequestSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.description("Unit of Measure creation request");
        schema.addProperty("name", new Schema<>().type("string").description("Unit name (1-50 chars, required)").example("Kilogram"));
        schema.addProperty("description", new Schema<>().type("string").description("Unit description (max 200 chars, optional)").example("Base unit of mass in SI"));
        schema.addProperty("conversionFactorToBase", new Schema<>().type("number").description("Positive factor relative to the base unit (required)").example(1.000));
        schema.addProperty("uomStatusId", new Schema<>().type("integer").format("int64").description("Related status id (required by business flow)").example(1));
        schema.addRequiredItem("name");
        schema.addRequiredItem("conversionFactorToBase");
        schema.addRequiredItem("uomStatusId");
        return schema;
    }

    private Schema<?> createUomUpdateSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.description("Unit of Measure update request. Note: uomStatusId cannot be changed here; use the change-state endpoint instead");
        schema.addProperty("name", new Schema<>().type("string").description("Unit name (1-50 chars, required)").example("Gram"));
        schema.addProperty("description", new Schema<>().type("string").description("Unit description (max 200 chars, optional)").example("Derived mass unit equal to one thousandth of a kilogram"));
        schema.addProperty("conversionFactorToBase", new Schema<>().type("number").description("Positive factor relative to the base unit (required)").example(0.001));
        schema.addRequiredItem("name");
        schema.addRequiredItem("conversionFactorToBase");
        return schema;
    }

    private Schema<?> createUomPageResponseSchema() {
        Schema<?> schema = new Schema<>();
        schema.type("object");
        schema.description("Paginated response for Unit of Measure resources");
        schema.addProperty("content", new ArraySchema()
            .items(new Schema<>().$ref("#/components/schemas/UomResponse"))
            .description("Page content"));
        schema.addProperty("totalElements", new Schema<>().type("integer").format("int64").example(2));
        schema.addProperty("totalPages", new Schema<>().type("integer").example(1));
        schema.addProperty("number", new Schema<>().type("integer").example(0));
        schema.addProperty("size", new Schema<>().type("integer").example(20));
        schema.addProperty("numberOfElements", new Schema<>().type("integer").example(2));
        schema.addProperty("first", new Schema<>().type("boolean").example(true));
        schema.addProperty("last", new Schema<>().type("boolean").example(true));
        schema.addProperty("empty", new Schema<>().type("boolean").example(false));
        return schema;
    }
}
