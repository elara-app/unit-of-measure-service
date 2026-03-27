package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ApplicationContextHolder;
import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final int code;
    private final String value;
    private final String message;

    public BaseException(ErrorCode errorCode, String message) {
        this.code = errorCode.getCode();
        this.value = errorCode.getValue();
        this.message = message;
    }

}