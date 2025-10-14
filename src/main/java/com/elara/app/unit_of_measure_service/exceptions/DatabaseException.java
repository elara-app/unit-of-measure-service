package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;

public class DatabaseException extends BaseException {
    public DatabaseException(Object... args) {
        super(ErrorCode.DATABASE_ERROR, args);
    }
}
