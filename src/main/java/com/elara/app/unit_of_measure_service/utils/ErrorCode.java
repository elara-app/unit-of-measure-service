package com.elara.app.unit_of_measure_service.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    DATABASE_ERROR(1001, "DATABASE_ERROR", "app.error.database"),
    INVALID_DATA(1002, "INVALID_DATA", "uom.error.invalid.data"),
    RESOURCE_CONFLICT(1003, "RESOURCE_CONFLICT", "uom.error.resource.conflict"),
    RESOURCE_NOT_FOUND(1004, "RESOURCE_NOT_FOUND", "uom.error.resource.not.found"),
    SERVICE_UNAVAILABLE(1005, "SERVICE_UNAVAILABLE", "app.error.service.unavailable"),
    UNEXPECTED_ERROR(1006, "UNEXPECTED_ERROR", "app.error.unexpected");

    private final int code;
    private final String value;
    private final String key;

}
