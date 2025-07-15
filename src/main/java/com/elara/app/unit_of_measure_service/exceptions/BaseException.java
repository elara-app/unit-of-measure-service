package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final int code;
    private final String value;
    private final String message;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.value = errorCode.getValue();
        this.message = errorCode.getMessage();
    }

    public BaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage != null ? errorCode.getMessage() + ": " + customMessage : errorCode.getMessage());
        this.code = errorCode.getCode();
        this.value = errorCode.getValue();
        this.message = customMessage != null ? errorCode.getMessage() + ": " + customMessage : errorCode.getMessage();
    }
}
