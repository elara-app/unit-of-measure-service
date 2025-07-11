package com.elara.app.unit_of_measure_service.config;

import com.elara.app.unit_of_measure_service.exceptions.DatabaseException;
import com.elara.app.unit_of_measure_service.exceptions.InvalidDataException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceConflictException;
import com.elara.app.unit_of_measure_service.exceptions.ResourceNotFoundException;
import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandlerTest.class
})
@ComponentScan(basePackages = "com.elara.app.unit_of_measure_service.config")
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @GetMapping("/database-exception")
        public void throwDatabaseException() {
            throw new DatabaseException("Database connection failed");
        }

        @GetMapping("/invalid-data-exception")
        public void throwBadRequestException() {
            throw new InvalidDataException("Invalid data");
        }

        @GetMapping("/resource-conflict-exception")
        public void throwResourceConflictException() {
            throw new ResourceConflictException("Resource already exists");
        }

        @GetMapping("/resource-not-found-exception")
        public void throwResourceNotFoundException() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @PostMapping("/validation-exception")
        public void throwValidationException(@Valid @RequestBody TestRequest request) {
            // This will throw a validation exception if the request is invalid
        }

        @GetMapping("/missing-param")
        public void throwMissingParameterException(@RequestParam("requiredParam") String param) {
            // Simulate a missing parameter
        }

        @PostMapping("/method-not-supported")
        public void throwMethodNotSupportedException() {
            // Will be triggered by sending a GET request to this endpoint
        }

        @GetMapping("/generic-exception")
        public void throwGenericException() {
            throw new RuntimeException("An unexpected error occurred");
        }
    }

    record TestRequest(@NotBlank String name) {
    }

    @Test
    void shouldHandleDatabaseException() throws Exception {
        mockMvc.perform(get("/test/database-exception").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATABASE_ERROR.getCode()))
                .andExpect(jsonPath("$.value").value(ErrorCode.DATABASE_ERROR.getValue()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATABASE_ERROR.getMessage() + " - Database connection failed"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/database-exception"));
    }

    @Test
    void shouldHandleInvalidDataException() throws Exception {
        mockMvc.perform(get("/test/invalid-data-exception").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_DATA.getCode()))
                .andExpect(jsonPath("$.value").value(ErrorCode.INVALID_DATA.getValue()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_DATA.getMessage() + " - Invalid data"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/invalid-data-exception"));
    }

    @Test
    void handleResourceConflictException() throws Exception {
        mockMvc.perform(get("/test/resource-conflict-exception").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_CONFLICT.getCode()))
                .andExpect(jsonPath("$.value").value(ErrorCode.RESOURCE_CONFLICT.getValue()))
                .andExpect(jsonPath("$.message").value(ErrorCode.RESOURCE_CONFLICT.getMessage() + " - Resource already exists"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/resource-conflict-exception"));
    }

    @Test
    void handleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/resource-not-found-exception").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.value").value(ErrorCode.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(ErrorCode.RESOURCE_NOT_FOUND.getMessage() + " - Resource not found"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/resource-not-found-exception"));
    }

    @Test
    void shouldHandleValidationException() throws Exception {
        String invalidJson = "{\"name\": \"\"}";
        mockMvc.perform(post("/test/validation-exception")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_DATA.getCode()))
                .andExpect(jsonPath("$.value").value(ErrorCode.INVALID_DATA.getValue()))
                .andExpect(jsonPath("$.message").value("name: must not be blank"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/validation-exception"));
    }

    @Test
    void shouldHandleMissingParameterException() throws Exception {
        mockMvc.perform(get("/test/missing-param").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_DATA.getCode()))
                .andExpect(jsonPath("$.value").value(ErrorCode.INVALID_DATA.getValue()))
                .andExpect(jsonPath("$.message").value("Missing parameter: requiredParam"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/missing-param"));
    }

    @Test
    void shouldHandleMethodNotSupportedException() throws Exception {
        mockMvc.perform(get("/test/method-not-supported").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_DATA.getCode()))
                .andExpect(jsonPath("$.value").value(ErrorCode.INVALID_DATA.getValue()))
                .andExpect(jsonPath("$.message").value(startsWith("HTTP method not supported")))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/method-not-supported"));
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic-exception").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNEXPECTED_ERROR.getCode()))
                .andExpect(jsonPath("$.value").value(ErrorCode.UNEXPECTED_ERROR.getValue()))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/generic-exception"));
    }

}