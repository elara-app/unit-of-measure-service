package com.elara.app.unit_of_measure_service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private MessageService messageService;

    private static final String TEST_KEY = "uom.error.invalid.data";
    private static final String ERROR_KEY = "app.error.message.not.found";
    private static final String TEST_MESSAGE = "Provided data is invalid";
    private static final String ERROR_MESSAGE = "An error occurred. Please try again.";
    private static final Locale DEFAULT_LOCALE = Locale.US;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("Get message with valid key, returns message")
    void getMessage_withValidKey_returnsMessage() {
        Object[] args = {"arg1", "arg2"};
        when(messageSource.getMessage(TEST_KEY, args, DEFAULT_LOCALE)).thenReturn(TEST_MESSAGE);
        String result = messageService.getMessage(TEST_KEY, args);
        assertEquals(TEST_MESSAGE, result);
        verify(messageSource).getMessage(TEST_KEY, args, DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("Get message with invalid key, returns error message")
    void getMessage_withInvalidKey_returnsErrorMessage() {
        Object[] args = new Object[]{"arg1"};
        when(messageSource.getMessage(TEST_KEY, args, DEFAULT_LOCALE)).thenThrow(new NoSuchMessageException(TEST_KEY));
        when(messageSource.getMessage(ERROR_KEY, null, DEFAULT_LOCALE)).thenReturn(ERROR_MESSAGE);
        String result = messageService.getMessage(TEST_KEY, args);
        assertEquals(ERROR_MESSAGE, result);
        verify(messageSource).getMessage(TEST_KEY, args, DEFAULT_LOCALE);
        verify(messageSource).getMessage(ERROR_KEY, null, DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("Get message wth error code and valid key, returns message")
    void getMessage_withErrorCodeAndValidKey_returnsMessage() {
        ErrorCode errorCode = ErrorCode.INVALID_DATA;
        Object[] args = new Object[]{"arg1"};
        when(messageSource.getMessage(TEST_KEY, args, DEFAULT_LOCALE)).thenReturn(TEST_MESSAGE);
        String result = messageService.getMessage(errorCode, args);
        assertEquals(TEST_MESSAGE, result);
        verify(messageSource).getMessage(TEST_KEY, args, DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("Get message with error code and invalid key, returns error message")
    void getMessage_withErrorCodeAndInvalidKey_returnsErrorMessage() {
        ErrorCode errorCode = ErrorCode.INVALID_DATA;
        when(messageSource.getMessage(TEST_KEY, null, DEFAULT_LOCALE)).thenThrow(new NoSuchMessageException(TEST_KEY));
        when(messageSource.getMessage(ERROR_KEY, null, DEFAULT_LOCALE)).thenReturn(ERROR_MESSAGE);
        String result = messageService.getMessage(errorCode, (Object[]) null);
        assertEquals(ERROR_MESSAGE, result);
        verify(messageSource).getMessage(TEST_KEY, null, DEFAULT_LOCALE);
        verify(messageSource).getMessage(ERROR_KEY, null, DEFAULT_LOCALE);
    }

}