package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;

public class ServiceUnavailableException extends BaseException {
    public ServiceUnavailableException() {
        super(ErrorCode.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String customMessage) {
        super(ErrorCode.SERVICE_UNAVAILABLE, customMessage);
    }
}
