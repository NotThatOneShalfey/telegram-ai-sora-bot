package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.PanelHelper;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public abstract class AbstractSimpleMessagePanel {
    private final TgBot bot;

    public AbstractSimpleMessagePanel(TgBot bot) {
        this.bot = bot;
    }

    private void processSendMessageError(String chatId, TelegramApiException e) {
        log.error(e.getMessage());
        String errorMessage = """
                           \uD83D\uDEA7 Генерация временно недоступна \uD83D\uDEA7
                Мы уже работаем над этим - попробуйте чуть позже или обратитесь в поддержку @CreativeLabAI
                """;
        try {
            bot.execute(new SendMessage(chatId, errorMessage));
        } catch (TelegramApiException ex) {
            log.error("Во время обработки ошибка возникла ошибка!!!!! {}", e.getMessage());
        }
    }

    protected void executeSendMessage(UserSession session, String text, InlineKeyboardMarkup keyboard, boolean withBalance) {
        SendMessage sm = new SendMessage();
        sm.setChatId(session.getChatId());
        sm.setText(text);
        // Добавляем кнопки
        if (keyboard != null) {
            sm.setReplyMarkup(keyboard);
        }
        // Добавляем баланс
        if (withBalance) {
            PanelHelper.addQuotedBalanceToMessage(sm, session.getUser().getBalance());
        }
        try {
            bot.execute(sm);
        } catch (TelegramApiException e) {
            processSendMessageError(session.getChatId(), e);
        }
    }


}
