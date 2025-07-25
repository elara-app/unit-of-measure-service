package com.elara.app.unit_of_measure_service.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    public String getMessage(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return messageSource.getMessage("app.error.message.not.found", null, LocaleContextHolder.getLocale());
        }
    }

    public String getMessage(ErrorCode errorCode, Object... args) {
        try {
            return messageSource.getMessage(errorCode.getKey(), args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return messageSource.getMessage("app.error.message.not.found", null, LocaleContextHolder.getLocale());
        }
    }

}
