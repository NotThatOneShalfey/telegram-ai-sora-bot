package com.example.tgbot.domain.value;

/**
 * Канал запроса на генерацию: чат Telegram или веб-интерфейс.
 * Используется для разделения логики — в чат отправляем панели, в веб — результат только через polling.
 */
public enum TaskSource {
    CHAT,
    WEB
}
