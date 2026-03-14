package com.example.tgbot.util;

import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.domain.value.Operation;

/**
 * Формирует пользовательские сообщения об ошибках.
 */
public final class ErrorMessageHelper {

    private static final String SUPPORT = "Обратитесь в поддержку @CreativeLabAI";

    private ErrorMessageHelper() {}

    /** Сообщение для Telegram: операция не выполнена + понятное описание */
    public static String forTelegram(Operation operation, ErrorCode errorCode) {
        return "⚠ Не удалось выполнить %s. %s".formatted(
                operation.getDisplayName(), errorCode.getDescription());
    }

    /** Сообщение без указания операции (универсальное) */
    public static String forTelegram(ErrorCode errorCode) {
        return "⚠ %s".formatted(errorCode.getDescription());
    }
}
