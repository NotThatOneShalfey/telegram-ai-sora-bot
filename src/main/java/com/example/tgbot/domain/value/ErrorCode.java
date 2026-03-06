package com.example.tgbot.domain.value;

import lombok.Getter;

/**
 * Коды ошибок приложения. Используются для Telegram-сообщений и web API.
 */
@Getter
public enum ErrorCode {
    E001("Ошибка генерации видео (Kei AI API)", "E001"),
    E002("Ошибка генерации музыки (Kei AI API)", "E002"),
    E003("Ошибка генерации изображений (Kei AI API)", "E003"),
    E004("Недостаточный баланс", "E004"),
    E005("Невалидный запрос (JSON, параметры)", "E005"),
    E006("Ошибка загрузки файлов", "E006"),
    E007("Ошибка внешнего API (Kei AI)", "E007"),
    E008("Внутренняя ошибка сервера", "E008"),
    E009("Превышен лимит запросов", "E009"),
    E010("Ошибка при отправке результата в Telegram", "E010"),
    E011("Операция отменена или не удалась (callback failed)", "E011");

    private final String description;
    private final String code;

    ErrorCode(String description, String code) {
        this.description = description;
        this.code = code;
    }
}
