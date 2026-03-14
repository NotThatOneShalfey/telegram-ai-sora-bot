package com.example.tgbot.exception;

import lombok.Getter;

/**
 * Исключение при превышении допустимого размера файла.
 * Содержит сообщение для пользователя.
 */
@Getter
public class FileTooLargeException extends RuntimeException {

    private final String userMessage;

    public FileTooLargeException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }
}
