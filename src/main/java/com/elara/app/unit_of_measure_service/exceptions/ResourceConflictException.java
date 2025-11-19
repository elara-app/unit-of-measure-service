package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;

public class ResourceConflictException extends BaseException {
    public ResourceConflictException(Object... args) {
        super(ErrorCode.RESOURCE_CONFLICT, args);
    }
}
