package com.example.tgbot.util;

import com.example.tgbot.data.ErrorCode;
import com.example.tgbot.data.Operation;

/**
 * Формирует пользовательские сообщения об ошибках.
 */
public final class ErrorMessageHelper {

    private static final String SUPPORT = "Обратитесь в поддержку @CreativeLabAI";

    private ErrorMessageHelper() {}

    /** Сообщение для Telegram: операция не выполнена + код ошибки */
    public static String forTelegram(Operation operation, ErrorCode errorCode) {
        return "⚠ Не удалось выполнить %s. Код ошибки: %s. %s".formatted(
                operation.getDisplayName(), errorCode.getCode(), SUPPORT);
    }

    /** Сообщение без указания операции (универсальное) */
    public static String forTelegram(ErrorCode errorCode) {
        return "⚠ Операция не выполнена. Код ошибки: %s. %s".formatted(errorCode.getCode(), SUPPORT);
    }
}
