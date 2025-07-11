package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String customMessage) {
        super(ErrorCode.RESOURCE_NOT_FOUND, customMessage);
    }
}
