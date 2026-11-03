package com.example.simulation.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageHelper {
    private static MessageSource messageSource;

    public MessageHelper(MessageSource messageSource) {
        MessageHelper.messageSource = messageSource;
    }

    public static String get(String key) {
        return messageSource.getMessage(
            key,
            null,
            Locale.getDefault()
        );
    }
}