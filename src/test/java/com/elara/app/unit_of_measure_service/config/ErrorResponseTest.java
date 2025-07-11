package com.elara.app.unit_of_measure_service.config;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorResponseTest {

    @Test
    @DisplayName("Builder should create ErrorResponse with all fields")
    void shouldCreateErrorResponseWithAllFields() {
        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .value(errorCode.getValue())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.MIN)
                .path("")
                .build();
        assertNotNull(errorResponse);
        assertEquals(errorCode.getCode(), errorResponse.getCode());
        assertEquals(errorCode.getValue(), errorResponse.getValue());
        assertEquals(errorCode.getMessage(), errorResponse.getMessage());
        assertEquals(LocalDateTime.MIN, errorResponse.getTimestamp());
        assertEquals("", errorResponse.getPath());
    }

}