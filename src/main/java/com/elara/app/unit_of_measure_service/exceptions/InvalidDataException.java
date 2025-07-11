package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;

public class InvalidDataException extends BaseException {
    public InvalidDataException() {
        super(ErrorCode.INVALID_DATA);
    }

    public InvalidDataException(String customMessage) {
        super(ErrorCode.INVALID_DATA, customMessage);
    }
}
