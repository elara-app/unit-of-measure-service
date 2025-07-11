package com.elara.app.unit_of_measure_service.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    DATABASE_ERROR(1001, "DATABASE_ERROR", "A database error occurred"),
    INVALID_DATA(1002, "INVALID_DATA", "Provided data is invalid"),
    RESOURCE_CONFLICT(1003, "RESOURCE_CONFLICT", "Resource conflict occurred"),
    RESOURCE_NOT_FOUND(1004, "RESOURCE_NOT_FOUND", "Resource not found"),
    SERVICE_UNAVAILABLE(1005, "SERVICE_UNAVAILABLE", "Service is currently unavailable"),
    UNEXPECTED_ERROR(1006, "UNEXPECTED_ERROR", "An unexpected error occurred");

    private final int code;
    private final String value;
    private final String message;

}
