package com.example.tgbot.telegram.handlers;

import com.example.tgbot.telegram.TgBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class InvoiceHandler {

    public void handlePreCheckoutQuery(String preCheckoutQueryId, TgBot tgBot) {
        // Подтверждаем оплату
        long execTime = 0L;
        try {
            AnswerPreCheckoutQuery answer = AnswerPreCheckoutQuery.builder()
                    .preCheckoutQueryId(preCheckoutQueryId)
                    .ok(true)
                    .build();
            long startTime = System.currentTimeMillis();
            Object o = tgBot.execute(answer);
            log.trace("Execute answer = {}", o.toString());
            long endTime = System.currentTimeMillis();
            execTime = endTime - startTime;
            log.trace("AnswerPreCheckoutQuery: {}", answer);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке подтверждения", e);
        } finally {
            log.trace("Exec time in millis: {}", execTime);
        }
    }
}
