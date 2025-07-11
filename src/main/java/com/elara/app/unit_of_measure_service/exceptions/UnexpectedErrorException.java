package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;

public class UnexpectedErrorException extends BaseException {
    public UnexpectedErrorException() {
        super(ErrorCode.UNEXPECTED_ERROR);
    }

    public UnexpectedErrorException(String customMessage) {
        super(ErrorCode.UNEXPECTED_ERROR, customMessage);
    }
}
