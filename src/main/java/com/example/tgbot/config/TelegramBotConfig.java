package com.example.tgbot.config;

import com.example.tgbot.bot.SoraVideoBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Конфигурация Telegram‑бота. Всегда регистрирует бота как webhook‑бота.
 */
@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.webhook-base-url:}")
    private String webhookBaseUrl;

    @Bean
    public TelegramBotsApi telegramBotsApi(SoraVideoBot soraVideoBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

        // Формируем полный URL: если базовый URL задан, то дополняем его путём бота.
        // Иначе используем только путь бота – в этом случае Telegram не сможет доставлять обновления,
        // но приложение можно тестировать локально.
        String base = webhookBaseUrl != null ? webhookBaseUrl.trim() : "";
        String fullWebhookUrl = base.isEmpty()
                ? soraVideoBot.getBotPath()
                : base + soraVideoBot.getBotPath();

        SetWebhook setWebhook = SetWebhook.builder().url(fullWebhookUrl).build();

        // Регистрируем webhook‑бота. Этот метод принимает экземпляр класса,
        // наследующего TelegramWebhookBot, поэтому преобразование к LongPollingBot не требуется.
        api.registerBot(soraVideoBot, setWebhook);
        return api;
    }
}
