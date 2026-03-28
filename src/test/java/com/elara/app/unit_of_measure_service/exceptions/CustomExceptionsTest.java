package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Custom Exception Classes")
class CustomExceptionsTest {

    @Nested
    @DisplayName("InvalidDataException")
    class InvalidDataExceptionTests {

        @Test
        @DisplayName("Constructor sets INVALID_DATA error code and message")
        void constructor_setsInvalidDataErrorCodeAndMessage() {
            String message = "Invalid input provided";

            InvalidDataException exception = new InvalidDataException(message);

            assertThat(exception).isNotNull();
            assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_DATA.getCode());
            assertThat(exception.getValue()).isEqualTo(ErrorCode.INVALID_DATA.getValue());
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Constructor with different message creates distinct exception")
        void constructor_withDifferentMessage_createsDistinctException() {
            String message1 = "Field 'name' is required";
            String message2 = "Field 'code' must be unique";

            InvalidDataException exception1 = new InvalidDataException(message1);
            InvalidDataException exception2 = new InvalidDataException(message2);

            assertThat(exception1.getMessage()).isEqualTo(message1);
            assertThat(exception2.getMessage()).isEqualTo(message2);
            assertThat(exception1.getCode()).isEqualTo(exception2.getCode());
        }

        @Test
        @DisplayName("Exception extends BaseException")
        void exceptionExtendsBaseException() {
            InvalidDataException exception = new InvalidDataException("Test");

            assertThat(exception).isInstanceOf(BaseException.class);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("DatabaseException")
    class DatabaseExceptionTests {

        @Test
        @DisplayName("Constructor sets DATABASE_ERROR error code and message")
        void constructor_setsDatabaseErrorCodeAndMessage() {
            String message = "Database connection failed";

            DatabaseException exception = new DatabaseException(message);

            assertThat(exception).isNotNull();
            assertThat(exception.getCode()).isEqualTo(ErrorCode.DATABASE_ERROR.getCode());
            assertThat(exception.getValue()).isEqualTo(ErrorCode.DATABASE_ERROR.getValue());
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Constructor with SQL error message creates exception")
        void constructor_withSqlErrorMessage_createsException() {
            String message = "ERROR: duplicate key value violates unique constraint";

            DatabaseException exception = new DatabaseException(message);

            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCode()).isEqualTo(ErrorCode.DATABASE_ERROR.getCode());
        }

        @Test
        @DisplayName("Exception extends BaseException")
        void exceptionExtendsBaseException() {
            DatabaseException exception = new DatabaseException("Test");

            assertThat(exception).isInstanceOf(BaseException.class);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("ServiceUnavailableException")
    class ServiceUnavailableExceptionTests {

        @Test
        @DisplayName("Constructor sets SERVICE_UNAVAILABLE error code and message")
        void constructor_setsServiceUnavailableErrorCodeAndMessage() {
            String message = "External service is unavailable";

            ServiceUnavailableException exception = new ServiceUnavailableException(message);

            assertThat(exception).isNotNull();
            assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_UNAVAILABLE.getCode());
            assertThat(exception.getValue()).isEqualTo(ErrorCode.SERVICE_UNAVAILABLE.getValue());
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Constructor with timeout message creates exception")
        void constructor_withTimeoutMessage_createsException() {
            String message = "Service timeout after 30 seconds";

            ServiceUnavailableException exception = new ServiceUnavailableException(message);

            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_UNAVAILABLE.getCode());
        }

        @Test
        @DisplayName("Exception extends BaseException")
        void exceptionExtendsBaseException() {
            ServiceUnavailableException exception = new ServiceUnavailableException("Test");

            assertThat(exception).isInstanceOf(BaseException.class);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("Constructor sets RESOURCE_NOT_FOUND error code and message")
        void constructor_setsResourceNotFoundErrorCodeAndMessage() {
            String message = "UOM not found with id: 123";

            ResourceNotFoundException exception = new ResourceNotFoundException(message);

            assertThat(exception).isNotNull();
            assertThat(exception.getCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.getCode());
            assertThat(exception.getValue()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.getValue());
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Exception extends BaseException")
        void exceptionExtendsBaseException() {
            ResourceNotFoundException exception = new ResourceNotFoundException("Test");

            assertThat(exception).isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("ResourceConflictException")
    class ResourceConflictExceptionTests {

        @Test
        @DisplayName("Constructor sets RESOURCE_CONFLICT error code and message")
        void constructor_setsResourceConflictErrorCodeAndMessage() {
            String message = "UOM code already exists";

            ResourceConflictException exception = new ResourceConflictException(message);

            assertThat(exception).isNotNull();
            assertThat(exception.getCode()).isEqualTo(ErrorCode.RESOURCE_CONFLICT.getCode());
            assertThat(exception.getValue()).isEqualTo(ErrorCode.RESOURCE_CONFLICT.getValue());
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Exception extends BaseException")
        void exceptionExtendsBaseException() {
            ResourceConflictException exception = new ResourceConflictException("Test");

            assertThat(exception).isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("UnexpectedErrorException")
    class UnexpectedErrorExceptionTests {

        @Test
        @DisplayName("Constructor sets UNEXPECTED_ERROR error code and message")
        void constructor_setsUnexpectedErrorCodeAndMessage() {
            String message = "An unexpected error occurred";

            UnexpectedErrorException exception = new UnexpectedErrorException(message);

            assertThat(exception).isNotNull();
            assertThat(exception.getCode()).isEqualTo(ErrorCode.UNEXPECTED_ERROR.getCode());
            assertThat(exception.getValue()).isEqualTo(ErrorCode.UNEXPECTED_ERROR.getValue());
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Exception extends BaseException")
        void exceptionExtendsBaseException() {
            UnexpectedErrorException exception = new UnexpectedErrorException("Test");

            assertThat(exception).isInstanceOf(BaseException.class);
        }
    }

}
