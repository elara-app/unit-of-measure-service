package com.elara.app.unit_of_measure_service.exceptions;

import com.elara.app.unit_of_measure_service.utils.ApplicationContextHolder;
import com.elara.app.unit_of_measure_service.utils.ErrorCode;
import com.elara.app.unit_of_measure_service.utils.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MockitoExtension.class)
class BaseExceptionTest {

    @Mock
    private MessageService messageService;

    @Nested
    @DisplayName("Constructor with ErrorCode only")
    class ConstructorWithErrorCodeOnly {

        @Test
        @DisplayName("Should create exception with localized message when MessageService is available")
        void shouldCreateExceptionWithLocalizedMessage() {
            String expectedMessage = "";
            when(messageService.getMessage(ErrorCode.DATABASE_ERROR)).thenReturn(expectedMessage);

            try (MockedStatic<ApplicationContextHolder> mockedHolder = mockStatic(ApplicationContextHolder.class)) {
                mockedHolder.when(() -> ApplicationContextHolder.getBean(MessageService.class)).thenReturn(messageService);

                BaseException exception = new BaseException(ErrorCode.DATABASE_ERROR);
                assertNotNull(exception);
                assertEquals(ErrorCode.DATABASE_ERROR.getCode(), exception.getCode());
                assertEquals(ErrorCode.DATABASE_ERROR.getValue(), exception.getValue());
                assertEquals(expectedMessage, exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should create exception with ErrorCode value when MessageService fails\n")
        void shouldCreateExceptionWithDefaultMessageWhenMessageServiceIsNotAvailable() {
            try (MockedStatic<ApplicationContextHolder> mockedHolder = mockStatic(ApplicationContextHolder.class)) {
                mockedHolder.when(() -> ApplicationContextHolder.getBean(MessageService.class)).thenReturn(new RuntimeException("MessageService is not available"));

                BaseException exception = new BaseException(ErrorCode.INVALID_DATA);
                assertEquals(ErrorCode.INVALID_DATA.getCode(), exception.getCode());
                assertEquals(ErrorCode.INVALID_DATA.getValue(), exception.getValue());
                assertEquals(ErrorCode.INVALID_DATA.getValue(), exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should pass arguments to MessageService")
        void shouldPassArgumentsToMessageService() {
            Object[] args = {"param1", 123, "param2"};
            String expectedMessage = "Localized message with params: param1, 123, param2";
            when(messageService.getMessage(eq(ErrorCode.RESOURCE_NOT_FOUND), eq(args))).thenReturn(expectedMessage);

            try (MockedStatic<ApplicationContextHolder> mockedHolder = mockStatic(ApplicationContextHolder.class)) {
                mockedHolder.when(() -> ApplicationContextHolder.getBean(MessageService.class)).thenReturn(messageService);

                BaseException exception = new BaseException(ErrorCode.RESOURCE_NOT_FOUND, args);
                assertEquals(expectedMessage, exception.getMessage());
                verify(messageService).getMessage(ErrorCode.RESOURCE_NOT_FOUND, args);
            }
        }

        @Nested
        @DisplayName("Subclass constructors")
        class SubclassConstructors {

            static Stream<Supplier<BaseException>> exceptionSuppliers() {
                return Stream.of(
                        DatabaseException::new,
                        () -> new DatabaseException("custom"),
                        InvalidDataException::new,
                        () -> new InvalidDataException("custom"),
                        ResourceConflictException::new,
                        () -> new ResourceConflictException("custom"),
                        ResourceNotFoundException::new,
                        () -> new ResourceNotFoundException("custom"),
                        ServiceUnavailableException::new,
                        () -> new ServiceUnavailableException("custom"),
                        UnexpectedErrorException::new,
                        () -> new UnexpectedErrorException("custom")
                );
            }

            @ParameterizedTest(name = "#{index} -> constructor {0}")
            @MethodSource("exceptionSuppliers")
            void shouldInstantiateAll(Supplier<BaseException> supplier) {
                when(messageService.getMessage(any(ErrorCode.class), any())).thenReturn("msg");

                try (MockedStatic<ApplicationContextHolder> mockedHolder = mockStatic(ApplicationContextHolder.class)) {
                    mockedHolder.when(() -> ApplicationContextHolder.getBean(MessageService.class)).thenReturn(messageService);

                    BaseException ex = supplier.get();
                    assertNotNull(ex);
                }
            }
        }
    }

    @Nested
    @DisplayName("Constructor with ErrorCode and custom message")
    class ConstructorWithCustomMessage {

        @Test
        @DisplayName("Should append custom message to localized message")
        void shouldAppendCustomMessageToLocalizedMessage() {
            String localizedMessage = "Resource conflict occurred";
            String customMessage = "Duplicate entry for key 'unique_constraint'";
            String expectedMessage = localizedMessage + ": " + customMessage;
            when(messageService.getMessage(ErrorCode.RESOURCE_CONFLICT))
                    .thenReturn(localizedMessage);

            try (MockedStatic<ApplicationContextHolder> mockedHolder = mockStatic(ApplicationContextHolder.class)) {
                mockedHolder.when(() -> ApplicationContextHolder.getBean(MessageService.class))
                        .thenReturn(messageService);

                BaseException exception = new BaseException(ErrorCode.RESOURCE_CONFLICT, customMessage);
                assertEquals(ErrorCode.RESOURCE_CONFLICT.getCode(), exception.getCode());
                assertEquals(ErrorCode.RESOURCE_CONFLICT.getValue(), exception.getValue());
                assertEquals(expectedMessage, exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should use only localized message when custom message is null")
        void shouldUseOnlyLocalizedMessageWhenCustomMessageIsNull() {
            String localizedMessage = "Service unavailable";
            when(messageService.getMessage(ErrorCode.SERVICE_UNAVAILABLE)).thenReturn(localizedMessage);

            try (MockedStatic<ApplicationContextHolder> mockedHolder = mockStatic(ApplicationContextHolder.class)) {
                mockedHolder.when(() -> ApplicationContextHolder.getBean(MessageService.class))
                        .thenReturn(messageService);

                BaseException exception = new BaseException(ErrorCode.SERVICE_UNAVAILABLE, (String) null);
                assertEquals(localizedMessage, exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should handle empty custom message")
        void shouldHandleEmptyCustomMessage() {
            // Given
            String localizedMessage = "Unexpected error";
            String customMessage = "";
            String expectedMessage = localizedMessage + ": " + customMessage;

            when(messageService.getMessage(ErrorCode.UNEXPECTED_ERROR))
                    .thenReturn(localizedMessage);

            try (MockedStatic<ApplicationContextHolder> mockedHolder = mockStatic(ApplicationContextHolder.class)) {
                mockedHolder.when(() -> ApplicationContextHolder.getBean(MessageService.class))
                        .thenReturn(messageService);

                // When
                BaseException exception = new BaseException(ErrorCode.UNEXPECTED_ERROR, customMessage);

                // Then
                assertEquals(expectedMessage, exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should work with arguments and custom message")
        void shouldWorkWithArgumentsAndCustomMessage() {
            // Given
            Object[] args = {"user123", "profile"};
            String localizedMessage = "Invalid data for user123 in profile";
            String customMessage = "Missing required field: email";
            String expectedMessage = localizedMessage + ": " + customMessage;

            when(messageService.getMessage(eq(ErrorCode.INVALID_DATA), eq(args)))
                    .thenReturn(localizedMessage);

            try (MockedStatic<ApplicationContextHolder> mockedHolder = mockStatic(ApplicationContextHolder.class)) {
                mockedHolder.when(() -> ApplicationContextHolder.getBean(MessageService.class))
                        .thenReturn(messageService);

                // When
                BaseException exception = new BaseException(ErrorCode.INVALID_DATA, customMessage, args);

                // Then
                assertEquals(expectedMessage, exception.getMessage());
                verify(messageService).getMessage(ErrorCode.INVALID_DATA, args);
            }
        }
    }

}