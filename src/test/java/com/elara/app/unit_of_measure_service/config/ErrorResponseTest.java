package com.elara.app.unit_of_measure_service.config;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    @DisplayName("Builder should create ErrorResponse with all fields")
    void shouldCreateErrorResponseWithAllFields() {
        int expectedCode = 500;
        String expectedValue = "DATABASE_ERROR";
        String expectedMessage = "Test database error message";
        LocalDateTime expectedTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0);
        String expectedPath = "/api/test";

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(expectedCode)
                .value(expectedValue)
                .message(expectedMessage)
                .timestamp(expectedTimestamp)
                .path(expectedPath)
                .build();

        assertNotNull(errorResponse);
        assertEquals(expectedCode, errorResponse.getCode());
        assertEquals(expectedValue, errorResponse.getValue());
        assertEquals(expectedMessage, errorResponse.getMessage());
        assertEquals(expectedTimestamp, errorResponse.getTimestamp());
        assertEquals(expectedPath, errorResponse.getPath());
    }

    @Test
    @DisplayName("Builder should create ErrorResponse with ErrorCode")
    void shouldCreateErrorResponseWithErrorCode() {
        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;
        String testMessage = "Custom error message";
        LocalDateTime now = LocalDateTime.now();
        String testPath = "/api/users";

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .value(errorCode.getValue())
                .message(testMessage)
                .timestamp(now)
                .path(testPath)
                .build();

        assertNotNull(errorResponse);
        assertEquals(errorCode.getCode(), errorResponse.getCode());
        assertEquals(errorCode.getValue(), errorResponse.getValue());
        assertEquals(testMessage, errorResponse.getMessage());
        assertEquals(now, errorResponse.getTimestamp());
        assertEquals(testPath, errorResponse.getPath());
    }

    @Test
    @DisplayName("Builder should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(400)
                .value("INVALID_DATA")
                .message(null)
                .timestamp(null)
                .path(null)
                .build();

        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getCode());
        assertEquals("INVALID_DATA", errorResponse.getValue());
        assertNull(errorResponse.getMessage());
        assertNull(errorResponse.getTimestamp());
        assertNull(errorResponse.getPath());
    }

    @Test
    @DisplayName("Builder should handle empty strings")
    void shouldHandleEmptyStrings() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(404)
                .value("")
                .message("")
                .timestamp(LocalDateTime.MIN)
                .path("")
                .build();

        assertNotNull(errorResponse);
        assertEquals(404, errorResponse.getCode());
        assertEquals("", errorResponse.getValue());
        assertEquals("", errorResponse.getMessage());
        assertEquals(LocalDateTime.MIN, errorResponse.getTimestamp());
        assertEquals("", errorResponse.getPath());
    }

}