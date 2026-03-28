package com.elara.app.unit_of_measure_service.config;

import com.elara.app.unit_of_measure_service.exceptions.BaseException;
import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private static final String TEST_PATH = "/api/uom/test";

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn(TEST_PATH);
    }

    @AfterEach
    void tearDown() {
        reset(messageService, request);
    }

    @Nested
    @DisplayName("BaseException Handling")
    class BaseExceptionTests {

        @Test
        @DisplayName("Handle BaseException with INVALID_DATA error code, returns BAD_REQUEST")
        void handleBaseException_withInvalidDataCode_returnsBadRequest() {
            BaseException exception = new BaseException(ErrorCode.INVALID_DATA, "Invalid input data provided");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.INVALID_DATA.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.INVALID_DATA.getValue());
            assertThat(body.getMessage()).isEqualTo("Invalid input data provided");
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
            assertThat(body.getTimestamp()).isNotNull()
                .isBeforeOrEqualTo(LocalDateTime.now())
                .isAfter(LocalDateTime.now().minusSeconds(2));
        }

        @Test
        @DisplayName("Handle BaseException with RESOURCE_CONFLICT error code, returns CONFLICT")
        void handleBaseException_withResourceConflictCode_returnsConflict() {
            BaseException exception = new BaseException(ErrorCode.RESOURCE_CONFLICT, "UOM code already exists");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.RESOURCE_CONFLICT.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.RESOURCE_CONFLICT.getValue());
            assertThat(body.getMessage()).isEqualTo("UOM code already exists");
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
        }

        @Test
        @DisplayName("Handle BaseException with RESOURCE_NOT_FOUND error code, returns NOT_FOUND")
        void handleBaseException_withResourceNotFoundCode_returnsNotFound() {
            BaseException exception = new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "UOM not found with id: 123");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.getValue());
            assertThat(body.getMessage()).isEqualTo("UOM not found with id: 123");
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
        }

        @Test
        @DisplayName("Handle BaseException with DATABASE_ERROR error code, returns INTERNAL_SERVER_ERROR")
        void handleBaseException_withDatabaseErrorCode_returnsInternalServerError() {
            BaseException exception = new BaseException(ErrorCode.DATABASE_ERROR, "Database connection failed");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.DATABASE_ERROR.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.DATABASE_ERROR.getValue());
            assertThat(body.getMessage()).isEqualTo("Database connection failed");
        }

        @Test
        @DisplayName("Handle BaseException with UNEXPECTED_ERROR error code, returns INTERNAL_SERVER_ERROR")
        void handleBaseException_withUnexpectedErrorCode_returnsInternalServerError() {
            BaseException exception = new BaseException(ErrorCode.UNEXPECTED_ERROR, "An unexpected error occurred");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.UNEXPECTED_ERROR.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.UNEXPECTED_ERROR.getValue());
        }

        @Test
        @DisplayName("Handle BaseException with SERVICE_UNAVAILABLE error code, returns INTERNAL_SERVER_ERROR")
        void handleBaseException_withServiceUnavailableCode_returnsInternalServerError() {
            BaseException exception = new BaseException(ErrorCode.SERVICE_UNAVAILABLE, "External service unavailable");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.SERVICE_UNAVAILABLE.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.SERVICE_UNAVAILABLE.getValue());
        }
    }

    @Nested
    @DisplayName("Validation Exception Handling")
    class ValidationExceptionTests {

        @Test
        @DisplayName("Handle MethodArgumentNotValidException with validation errors, returns BAD_REQUEST")
        void handleValidationException_withValidationErrors_returnsInvalidDataError() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            Object[] messageArguments = new Object[]{"code", "Code field must not be empty"};
            when(exception.getDetailMessageArguments()).thenReturn(messageArguments);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.INVALID_DATA.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.INVALID_DATA.getValue());
            assertThat(body.getMessage()).isEqualTo("Code field must not be empty");
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
        }

        @Test
        @DisplayName("Handle ConstraintViolationException with message, returns BAD_REQUEST")
        void handleConstraintViolationException_withMessage_returnsInvalidDataError() {
            String errorMessage = "createUom.code: size must be between 1 and 20";
            ConstraintViolationException exception = mock(ConstraintViolationException.class);
            when(exception.getMessage()).thenReturn(errorMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.INVALID_DATA.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.INVALID_DATA.getValue());
            assertThat(body.getMessage()).isEqualTo(errorMessage);
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
        }

        @Test
        @DisplayName("Handle ConstraintViolationException with null message, returns BAD_REQUEST")
        void handleConstraintViolationException_withNullMessage_returnsBadRequest() {
            ConstraintViolationException exception = mock(ConstraintViolationException.class);
            when(exception.getMessage()).thenReturn(null);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.INVALID_DATA.getCode());
            assertThat(body.getMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("Request Parameter Exception Handling")
    class RequestParameterExceptionTests {

        @Test
        @DisplayName("Handle MissingServletRequestParameterException with String parameter, returns BAD_REQUEST")
        void handleMissingParameter_withStringParameter_returnsBadRequest() {
            String parameterName = "userId";
            String translatedMessage = "Missing required parameter: userId";
            MissingServletRequestParameterException exception = 
                new MissingServletRequestParameterException(parameterName, "String");
            
            when(messageService.getMessage(eq("parameter.missing"), eq(parameterName))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleMissingParameter(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.INVALID_DATA.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.INVALID_DATA.getValue());
            assertThat(body.getMessage()).isEqualTo(translatedMessage);
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
            
            verify(messageService).getMessage(eq("parameter.missing"), eq(parameterName));
            verifyNoMoreInteractions(messageService);
        }

        @Test
        @DisplayName("Handle MissingServletRequestParameterException with Integer parameter, returns BAD_REQUEST")
        void handleMissingParameter_withIntegerParameter_returnsBadRequest() {
            String parameterName = "page";
            String translatedMessage = "Missing required parameter: page";
            MissingServletRequestParameterException exception = 
                new MissingServletRequestParameterException(parameterName, "Integer");
            
            when(messageService.getMessage(eq("parameter.missing"), eq(parameterName))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleMissingParameter(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).isEqualTo(translatedMessage);
            verify(messageService).getMessage(eq("parameter.missing"), eq(parameterName));
        }

        @Test
        @DisplayName("Handle HttpRequestMethodNotSupportedException with POST method, returns METHOD_NOT_ALLOWED")
        void handleMethodNotSupported_withPostMethod_returnsMethodNotAllowed() {
            String method = "POST";
            String translatedMessage = "Method POST is not supported for this endpoint";
            when(request.getMethod()).thenReturn(method);
            when(messageService.getMessage(eq("method.not.supported"), eq(method))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotSupported(request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.INVALID_DATA.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.INVALID_DATA.getValue());
            assertThat(body.getMessage()).isEqualTo(translatedMessage);
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
            
            verify(request).getRequestURI();
            verify(request).getMethod();
            verify(messageService).getMessage(eq("method.not.supported"), eq(method));
            verifyNoMoreInteractions(request, messageService);
        }

        @Test
        @DisplayName("Handle HttpRequestMethodNotSupportedException with DELETE method, returns METHOD_NOT_ALLOWED")
        void handleMethodNotSupported_withDeleteMethod_returnsMethodNotAllowed() {
            String method = "DELETE";
            String translatedMessage = "Method DELETE is not supported for this endpoint";
            when(request.getMethod()).thenReturn(method);
            when(messageService.getMessage(eq("method.not.supported"), eq(method))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotSupported(request);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody().getMessage()).isEqualTo(translatedMessage);
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException Handling")
    class DataIntegrityExceptionTests {

        @Test
        @DisplayName("Handle DataIntegrityViolationException with detail pattern, extracts detail and returns CONFLICT")
        void handleDataIntegrityViolationException_withDetailPattern_extractsDetailAndReturnsConflict() {
            String exceptionMessage = "could not execute statement; Detail: Key (code)=(UOM123) already exists.";
            String extractedDetail = "Key (code)=(UOM123) already exists";
            String translatedMessage = "The UOM code already exists in database";
            
            DataIntegrityViolationException exception = new DataIntegrityViolationException(exceptionMessage);
            when(messageService.getMessage(eq("global.error.database"), eq(extractedDetail))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.DATABASE_ERROR.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.DATABASE_ERROR.getValue());
            assertThat(body.getMessage()).startsWith("Integrity violation:").contains(translatedMessage);
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
            
            ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
            verify(messageService).getMessage(eq("global.error.database"), detailCaptor.capture());
            assertThat(detailCaptor.getValue()).isEqualTo(extractedDetail);
            verifyNoMoreInteractions(messageService);
        }

        @Test
        @DisplayName("Handle DataIntegrityViolationException without detail pattern, uses default placeholder")
        void handleDataIntegrityViolationException_withoutDetailPattern_usesDefaultPlaceholder() {
            String exceptionMessage = "Constraint violation occurred without specific details";
            String defaultDetail = "<>";
            String translatedMessage = "A database constraint was violated";
            
            DataIntegrityViolationException exception = new DataIntegrityViolationException(exceptionMessage);
            when(messageService.getMessage(eq("global.error.database"), eq(defaultDetail))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.DATABASE_ERROR.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.DATABASE_ERROR.getValue());
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
            
            ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
            verify(messageService).getMessage(eq("global.error.database"), detailCaptor.capture());
            assertThat(detailCaptor.getValue()).isEqualTo(defaultDetail);
        }

        @Test
        @DisplayName("Handle DataIntegrityViolationException with complex multiline detail, extracts correctly")
        void handleDataIntegrityViolationException_withComplexMultilineDetail_extractsCorrectly() {
            String exceptionMessage = "ERROR: duplicate key value violates unique constraint \"uom_code_key\"\n" +
                                      "Detail: Key (code)=(METER) already exists.\n" +
                                      "Additional context here";
            String extractedDetail = "Key (code)=(METER) already exists";
            String translatedMessage = "Duplicate UOM code METER";
            
            DataIntegrityViolationException exception = new DataIntegrityViolationException(exceptionMessage);
            when(messageService.getMessage(eq("global.error.database"), eq(extractedDetail))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            
            ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
            verify(messageService).getMessage(eq("global.error.database"), detailCaptor.capture());
            assertThat(detailCaptor.getValue()).isEqualTo(extractedDetail);
        }

        @Test
        @DisplayName("Handle DataIntegrityViolationException with whitespace in detail, trims correctly")
        void handleDataIntegrityViolationException_withWhitespaceInDetail_trimsCorrectly() {
            String exceptionMessage = "Error occurred\nDetail:   Key (id)=(123) already exists.  \nMore info";
            String extractedDetail = "Key (id)=(123) already exists";
            String translatedMessage = "Duplicate key error";
            
            DataIntegrityViolationException exception = new DataIntegrityViolationException(exceptionMessage);
            when(messageService.getMessage(eq("global.error.database"), eq(extractedDetail))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(exception, request);
            
            assertNotNull(response);
            ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
            verify(messageService).getMessage(eq("global.error.database"), detailCaptor.capture());
            assertThat(detailCaptor.getValue()).isEqualTo(extractedDetail);
        }

        @Test
        @DisplayName("Handle DataIntegrityViolationException with detail ending with period, extracts up to period")
        void handleDataIntegrityViolationException_withDetailEndingWithPeriod_extractsUpToPeriod() {
            String exceptionMessage = "Detail: Foreign key constraint failed. Additional info after period";
            String extractedDetail = "Foreign key constraint failed";
            String translatedMessage = "Foreign key error";
            
            DataIntegrityViolationException exception = new DataIntegrityViolationException(exceptionMessage);
            when(messageService.getMessage(eq("global.error.database"), eq(extractedDetail))).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(exception, request);
            
            assertNotNull(response);
            verify(messageService).getMessage(eq("global.error.database"), eq(extractedDetail));
        }
    }

    @Nested
    @DisplayName("Generic Exception Handling")
    class GenericExceptionTests {

        @Test
        @DisplayName("Handle generic RuntimeException with message, returns INTERNAL_SERVER_ERROR")
        void handleException_withRuntimeExceptionAndMessage_returnsInternalServerError() {
            RuntimeException exception = new RuntimeException("Null pointer occurred in processing");
            String translatedMessage = "An unexpected error occurred: Null pointer occurred in processing";
            
            when(messageService.getMessage(eq("global.error.unexpected"), eq(exception.getMessage())))
                .thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            ErrorResponse body = response.getBody();
            assertNotNull(body);
            assertThat(body.getCode()).isEqualTo(ErrorCode.UNEXPECTED_ERROR.getCode());
            assertThat(body.getValue()).isEqualTo(ErrorCode.UNEXPECTED_ERROR.getValue());
            assertThat(body.getMessage()).isEqualTo(translatedMessage);
            assertThat(body.getPath()).isEqualTo(TEST_PATH);
            
            verify(messageService).getMessage(eq("global.error.unexpected"), eq(exception.getMessage()));
            verifyNoMoreInteractions(messageService);
        }

        @Test
        @DisplayName("Handle Exception with null message, handles gracefully")
        void handleException_withNullMessage_handlesGracefully() {
            Exception exception = new RuntimeException();
            String translatedMessage = "An unexpected error occurred";
            
            when(messageService.getMessage(eq("global.error.unexpected"), isNull())).thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception, request);
            
            assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getMessage()).isEqualTo(translatedMessage);
            verify(messageService).getMessage(eq("global.error.unexpected"), isNull());
        }

        @Test
        @DisplayName("Handle IllegalStateException, returns INTERNAL_SERVER_ERROR")
        void handleException_withIllegalStateException_returnsInternalServerError() {
            IllegalStateException exception = new IllegalStateException("Service in invalid state");
            String translatedMessage = "An unexpected error occurred: Service in invalid state";
            
            when(messageService.getMessage(eq("global.error.unexpected"), eq(exception.getMessage())))
                .thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception, request);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getMessage()).isEqualTo(translatedMessage);
        }

        @Test
        @DisplayName("Handle NullPointerException, returns INTERNAL_SERVER_ERROR")
        void handleException_withNullPointerException_returnsInternalServerError() {
            NullPointerException exception = new NullPointerException("Cannot invoke method on null object");
            String translatedMessage = "An unexpected error occurred: Cannot invoke method on null object";
            
            when(messageService.getMessage(eq("global.error.unexpected"), eq(exception.getMessage())))
                .thenReturn(translatedMessage);
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception, request);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.UNEXPECTED_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("Error Response Structure and Consistency")
    class ErrorResponseStructureTests {

        @Test
        @DisplayName("Error response contains all required fields with proper values")
        void errorResponse_containsAllRequiredFieldsWithProperValues() {
            BaseException exception = new BaseException(ErrorCode.INVALID_DATA, "Test validation message");
            LocalDateTime beforeCall = LocalDateTime.now();
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            LocalDateTime afterCall = LocalDateTime.now();
            ErrorResponse errorResponse = response.getBody();
            
            assertNotNull(errorResponse);
            assertThat(errorResponse.getCode()).isPositive().isEqualTo(ErrorCode.INVALID_DATA.getCode());
            assertThat(errorResponse.getValue()).isNotBlank().isEqualTo(ErrorCode.INVALID_DATA.getValue());
            assertThat(errorResponse.getMessage()).isNotBlank().isEqualTo("Test validation message");
            assertThat(errorResponse.getTimestamp())
                .isNotNull()
                .isAfterOrEqualTo(beforeCall)
                .isBeforeOrEqualTo(afterCall);
            assertThat(errorResponse.getPath()).isNotBlank().isEqualTo(TEST_PATH);
        }

        @Test
        @DisplayName("Multiple consecutive error responses have different timestamps")
        void multipleErrorResponses_haveDifferentTimestamps() throws InterruptedException {
            BaseException exception1 = new BaseException(ErrorCode.INVALID_DATA, "First error");
            ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleBaseException(exception1, request);
            
            Thread.sleep(10);
            
            BaseException exception2 = new BaseException(ErrorCode.INVALID_DATA, "Second error");
            ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleBaseException(exception2, request);
            
            LocalDateTime timestamp1 = response1.getBody().getTimestamp();
            LocalDateTime timestamp2 = response2.getBody().getTimestamp();
            
            assertThat(timestamp1).isBefore(timestamp2);
        }

        @Test
        @DisplayName("All exception handlers preserve request URI in response")
        void allExceptionHandlers_preserveRequestUri() {
            BaseException baseException = new BaseException(ErrorCode.INVALID_DATA, "Error");
            ConstraintViolationException constraintException = mock(ConstraintViolationException.class);
            when(constraintException.getMessage()).thenReturn("Constraint error");
            Exception genericException = new RuntimeException("Generic error");
            
            when(messageService.getMessage(anyString(), anyString())).thenReturn("Message");
            
            assertThat(exceptionHandler.handleBaseException(baseException, request).getBody().getPath())
                .isEqualTo(TEST_PATH);
            assertThat(exceptionHandler.handleConstraintViolationException(constraintException, request).getBody().getPath())
                .isEqualTo(TEST_PATH);
            assertThat(exceptionHandler.handleException(genericException, request).getBody().getPath())
                .isEqualTo(TEST_PATH);
            
            verify(request, atLeast(3)).getRequestURI();
        }
    }

    @Nested
    @DisplayName("HTTP Status Code Mapping Logic")
    class HttpStatusCodeMappingTests {

        @Test
        @DisplayName("Error code 1002 maps to BAD_REQUEST")
        void errorCode1002_mapsToBadRequest() {
            BaseException exception = new BaseException(ErrorCode.INVALID_DATA, "Invalid");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Error code 1003 maps to CONFLICT")
        void errorCode1003_mapsToConflict() {
            BaseException exception = new BaseException(ErrorCode.RESOURCE_CONFLICT, "Conflict");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("Error code 1004 maps to NOT_FOUND")
        void errorCode1004_mapsToNotFound() {
            BaseException exception = new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "Not found");
            
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(exception, request);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Error codes 1001, 1005, 1006 map to INTERNAL_SERVER_ERROR")
        void databaseAndUnexpectedErrorCodes_mapToInternalServerError() {
            BaseException databaseException = new BaseException(ErrorCode.DATABASE_ERROR, "DB error");
            BaseException serviceException = new BaseException(ErrorCode.SERVICE_UNAVAILABLE, "Service down");
            BaseException unexpectedException = new BaseException(ErrorCode.UNEXPECTED_ERROR, "Unexpected");
            
            assertThat(exceptionHandler.handleBaseException(databaseException, request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(exceptionHandler.handleBaseException(serviceException, request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(exceptionHandler.handleBaseException(unexpectedException, request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Validation exceptions always return BAD_REQUEST")
        void validationExceptions_alwaysReturnBadRequest() {
            MethodArgumentNotValidException methodArgException = mock(MethodArgumentNotValidException.class);
            when(methodArgException.getDetailMessageArguments()).thenReturn(new Object[]{"field", "error"});
            ConstraintViolationException constraintException = mock(ConstraintViolationException.class);
            when(constraintException.getMessage()).thenReturn("Constraint error");
            MissingServletRequestParameterException missingParamException = 
                new MissingServletRequestParameterException("param", "String");
            
            when(messageService.getMessage(anyString(), anyString())).thenReturn("Message");
            
            assertThat(exceptionHandler.handleValidationException(methodArgException, request).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exceptionHandler.handleConstraintViolationException(constraintException, request).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exceptionHandler.handleMissingParameter(missingParamException, request).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("DataIntegrityViolationException always returns CONFLICT")
        void dataIntegrityViolationException_alwaysReturnsConflict() {
            DataIntegrityViolationException exception1 = new DataIntegrityViolationException("Detail: Key exists.");
            DataIntegrityViolationException exception2 = new DataIntegrityViolationException("No detail");
            
            when(messageService.getMessage(anyString(), anyString())).thenReturn("Message");
            
            assertThat(exceptionHandler.handleDataIntegrityViolationException(exception1, request).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
            assertThat(exceptionHandler.handleDataIntegrityViolationException(exception2, request).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("Generic Exception always returns INTERNAL_SERVER_ERROR")
        void genericException_alwaysReturnsInternalServerError() {
            Exception exception1 = new Exception("Generic error");
            Exception exception2 = new IllegalArgumentException("Illegal arg");
            
            when(messageService.getMessage(anyString(), anyString())).thenReturn("Message");
            
            assertThat(exceptionHandler.handleException(exception1, request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(exceptionHandler.handleException(exception2, request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
