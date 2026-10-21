package com.elara.app.unit_of_measure_service.config;

import com.elara.app.unit_of_measure_service.exceptions.BaseException;
import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception, HttpServletRequest request) {
        String path = request.getRequestURI();
        int code = exception.getCode();
        String message = exception.getMessage();
        log.warn("[GlobalExceptionHandler-BaseException] Code: {} | Message: {} | Path: {}", code, message, path);
        ErrorResponse errorResponse = createErrorResponse(
            code,
            exception.getValue(),
            message,
            path
        );
        return new ResponseEntity<>(errorResponse, determineHttpStatus(code));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.warn("[GlobalExceptionHandler-ValidationError] Message: {} | Path: {}",
            exception.getMessage(), path);
        ErrorResponse errorResponse = createErrorResponse(
            ErrorCode.INVALID_DATA.getCode(),
            ErrorCode.INVALID_DATA.getValue(),
            Arrays.stream(exception.getDetailMessageArguments()).toList().get(1).toString(),
            path
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception, HttpServletRequest request) {
        String path = request.getRequestURI();
        String message = exception.getMessage();
        log.warn("[GlobalExceptionHandler-ConstraintViolation] Message: {} | Path: {}", message, path);
        ErrorResponse errorResponse = createErrorResponse(
                ErrorCode.INVALID_DATA.getCode(),
                ErrorCode.INVALID_DATA.getValue(),
                message,
                path
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException exception, HttpServletRequest request) {
        String path = request.getRequestURI();
        String parameter = exception.getParameterName();
        log.warn("[GlobalExceptionHandler-MissingParameter] Parameter: {} | Path: {}", parameter, path);
        ErrorResponse errorResponse = createErrorResponse(
            ErrorCode.INVALID_DATA.getCode(),
            ErrorCode.INVALID_DATA.getValue(),
            messageService.getMessage("parameter.missing", parameter),
            path
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        log.warn("[GlobalExceptionHandler-MethodNotAllowed] Method: {} | Path: {}", method, path);
        ErrorResponse errorResponse = createErrorResponse(
            ErrorCode.INVALID_DATA.getCode(),
            ErrorCode.INVALID_DATA.getValue(),
            messageService.getMessage("method.not.supported", method),
            path
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exception, HttpServletRequest request) {
        String path = request.getRequestURI();
        String detail = getDataFromDataIntegrityExceptionMessage(exception.getMessage());
        log.error("[GlobalExceptionHandler-DataIntegrityViolation] Detail: {} | Path: {}",
            detail, path, exception);
        ErrorResponse errorResponse = createErrorResponse(
            ErrorCode.DATABASE_ERROR.getCode(),
            ErrorCode.DATABASE_ERROR.getValue(),
            "Integrity violation: " + messageService.getMessage("global.error.database", detail),
            path
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        String path = request.getRequestURI();
        String message = exception.getMessage();
        log.error("[GlobalExceptionHandler-UnexpectedException] Message: {} | Path: {}", message, path, exception);
        ErrorResponse errorResponse = createErrorResponse(
            ErrorCode.UNEXPECTED_ERROR.getCode(),
            ErrorCode.UNEXPECTED_ERROR.getValue(),
            messageService.getMessage("global.error.unexpected", message),
            path
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String getDataFromDataIntegrityExceptionMessage(String exceptionMessage) {
        String notAvailable = "<>";
        Matcher detailMatcher = Pattern.compile("Detail:\\s*([^.]+)").matcher(exceptionMessage);
        return detailMatcher.find() ? detailMatcher.group(1).trim() : notAvailable;
    }

    private ErrorResponse createErrorResponse(int code, String value, String message, String path) {
        return ErrorResponse.builder()
            .code(code)
            .value(value)
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(path)
            .build();
    }

    private HttpStatus determineHttpStatus(int errorCode) {
        return switch (errorCode) {
            case 1002 -> HttpStatus.BAD_REQUEST;
            case 1003 -> HttpStatus.CONFLICT;
            case 1004 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}
