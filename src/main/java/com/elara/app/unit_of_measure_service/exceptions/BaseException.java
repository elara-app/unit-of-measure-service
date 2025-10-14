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

    public BaseException(ErrorCode errorCode, Object... args) {
        this.code = errorCode.getCode();
        this.value = errorCode.getValue();
        this.message = getLocalizedMessage(errorCode, args);
    }

    private String getLocalizedMessage(ErrorCode errorCode, Object... args) {
        try {
            MessageService messageService = ApplicationContextHolder.getBean(MessageService.class);
            return messageService.getMessage(errorCode, args);
        } catch (Exception e) {
            return errorCode.getValue();
        }
    }

}