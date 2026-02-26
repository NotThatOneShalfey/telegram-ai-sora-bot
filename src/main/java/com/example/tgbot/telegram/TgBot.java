package com.example.tgbot.telegram;

import com.example.tgbot.telegram.handlers.CallbackHandler;
import com.example.tgbot.telegram.handlers.InvoiceHandler;
import com.example.tgbot.telegram.handlers.MessageHandler;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
public class TgBot extends TelegramWebhookBot {
    private final Executor taskExecutor;
    // Сессии юзеров с ключом по ChatId
    @Getter
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
    // Все доступные панели для отправки в чаты
    @Getter
    private final Map<String, IChatPanel> panels = new ConcurrentHashMap<>();
    private final CallbackHandler callbackHandler;
    private final MessageHandler messageHandler;
    private final InvoiceHandler invoiceHandler;

    @Getter
    @Value("${telegram.bot.payment-token:}")
    private String providerToken;

    // Инициализация бота с дефолт параметрами, botToken из .env
    public TgBot(@Value("${telegram.bot.token}") String botToken,
                 CallbackHandler callbackHandler,
                 MessageHandler messageHandler,
                 InvoiceHandler invoiceHandler,
                 @Qualifier("botExecutor") Executor taskExecutor) {
        super(botToken);
        this.callbackHandler = callbackHandler;
        this.messageHandler = messageHandler;
        this.invoiceHandler = invoiceHandler;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        log.debug("Update Received: {}", update);
        taskExecutor.execute(() -> {
            try {
                processUpdate(update);
            } catch (Exception e) {
                log.error("Unhandled exception while processing update", e);
            }
        });
        return null;
    }

    @Override
    public String getBotPath() {
        return null;
    }

    @Override
    public String getBotUsername() {
        return null;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    private void processUpdate(Update update) {
        log.trace("Call processUpdate");
        try {
            if (update.hasCallbackQuery()) {
                log.trace("update has CallbackQuery");
                callbackHandler.handleCallback(update.getCallbackQuery());
                return;
            }
            // Если подтверждение оплаты
            if (update.hasPreCheckoutQuery()) {
                log.trace("update has preCheckoutQuery");
                invoiceHandler.handlePreCheckoutQuery(update.getPreCheckoutQuery().getId());
                return;
            }
            // Если не callback и не оплата, то заходим в обработку сообщения
            if (update.hasMessage()) {
                log.trace("update has Message");
                messageHandler.handleMessage(update.getMessage());
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

//    @PostConstruct
//    private void postConstruct(Collection<IChatPanel> panelsCollection) {
//        // В Мапе ключ - getLabel(), вызванный у интерфейса
//        panelsCollection.forEach(icp -> panels.put(icp.getLabel(), icp));
//    }
}
