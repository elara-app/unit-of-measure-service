package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BaseExceptionTest {

    static class ExceptionProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(DatabaseException.class, ErrorCode.DATABASE_ERROR),
                    Arguments.of(InvalidDataException.class, ErrorCode.INVALID_DATA),
                    Arguments.of(ResourceConflictException.class, ErrorCode.RESOURCE_CONFLICT),
                    Arguments.of(ResourceNotFoundException.class, ErrorCode.RESOURCE_NOT_FOUND),
                    Arguments.of(ServiceUnavailableException.class, ErrorCode.SERVICE_UNAVAILABLE),
                    Arguments.of(UnexpectedErrorException.class, ErrorCode.UNEXPECTED_ERROR)
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ExceptionProvider.class)
    void shouldInitializeWithDefaultErrorCode(Class<? extends BaseException> exceptionClass, ErrorCode errorCode) throws Exception {
        // Test Default Constructor
        Constructor<? extends BaseException> constructor = exceptionClass.getConstructor();
        BaseException exception = constructor.newInstance();
        assertNotNull(exception);
        assertEquals(errorCode.getCode(), exception.getCode());
        assertEquals(errorCode.getValue(), exception.getValue());
        assertEquals(errorCode.getMessage(), exception.getMessage());
    }

    @ParameterizedTest
    @ArgumentsSource(ExceptionProvider.class)
    void shouldInitializeWithCustomMessage(Class<? extends BaseException> exceptionClass, ErrorCode errorCode) throws Exception {
        String customMessage = "Custom error details";
        Constructor<? extends BaseException> constructor = exceptionClass.getConstructor(String.class);
        BaseException exception = constructor.newInstance(customMessage);
        assertNotNull(exception);
        assertEquals(errorCode.getCode(), exception.getCode());
        assertEquals(errorCode.getValue(), exception.getValue());
        assertEquals(errorCode.getMessage() + " - " + customMessage, exception.getMessage());
    }

    @ParameterizedTest
    @ArgumentsSource(ExceptionProvider.class)
    void shouldHandleNullCustomMessage(Class<? extends BaseException> exceptionClass, ErrorCode errorCode) throws Exception {
        Constructor<? extends BaseException> constructor = exceptionClass.getConstructor(String.class);
        BaseException exception = constructor.newInstance((Object) null);
        assertNotNull(exception);
        assertEquals(errorCode.getCode(), exception.getCode());
        assertEquals(errorCode.getValue(), exception.getValue());
        assertEquals(errorCode.getMessage(), exception.getMessage());
    }

}