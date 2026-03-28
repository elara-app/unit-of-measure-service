package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;

public class UnexpectedErrorException extends BaseException {
    public UnexpectedErrorException(String message) {
        super(ErrorCode.UNEXPECTED_ERROR, message);
    }
}
