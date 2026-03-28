package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;

public class InvalidDataException extends BaseException {
    public InvalidDataException(String message) {
        super(ErrorCode.INVALID_DATA, message);
    }
}
