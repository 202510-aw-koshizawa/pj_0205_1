package com.example.todo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class I18nSmokeCheck implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(I18nSmokeCheck.class);

    private final MessageSource messageSource;

    public I18nSmokeCheck(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        logMessage("login.title", Locale.JAPANESE);
        logMessage("login.title", Locale.ENGLISH);
    }

    private void logMessage(String code, Locale locale) {
        try {
            String message = messageSource.getMessage(code, null, locale);
            logger.info("i18n check: code='{}' locale='{}' -> '{}'", code, locale, message);
        } catch (Exception ex) {
            logger.warn("i18n check failed: code='{}' locale='{}' -> {}", code, locale, ex.toString());
        }
    }
}
