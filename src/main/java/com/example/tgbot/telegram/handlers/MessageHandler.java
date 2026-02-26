package com.example.tgbot.telegram.handlers;

import com.example.tgbot.db.User;
import com.example.tgbot.models.adapters.IRequestAdapter;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.TelegramExecutor;
import com.example.tgbot.telegram.TelegramExecutorImpl;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.buttons.PaidPackageButton;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.impl.MainMenuPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageHandler {

    private final TelegramExecutor telegramExecutor;
    private final UserService userService;
    private final Map<GenerationModel, IRequestAdapter> adapters = new ConcurrentHashMap<>();

    public MessageHandler(UserService userService,
                          Collection<IRequestAdapter> adaptersCollection,
                          TelegramExecutor telegramExecutor) {
        this.userService = userService;
        this.telegramExecutor = telegramExecutor;
        adaptersCollection.forEach(a -> adapters.put(a.getModel(), a));

    }


    public void handleMessage(Message message) {
        // Получили айди чата
        Long chatId = message.getChatId();
        User user = userService.findOrCreateUser(chatId);
        UserSession session = telegramExecutor.getSessions().computeIfAbsent(chatId.toString(), id -> new com.example.tgbot.telegram.sessions.UserSession(user));
        // Обработка стартового сообщения
        if (message.hasText() && message.getText().contains("/start")) {
            String referral = null;
            if (message.getText().contains(" ")) {
                referral = message.getText().split(" ")[1];
            }
            handleStart(session, user, referral, message.getFrom().getUserName());
            return;
        }
        // Если завершение оплаты
        if (message.getSuccessfulPayment() != null) {
            userService.addBalance(user, PaidPackageButton.getPackagePriceByName(message.getSuccessfulPayment().getInvoicePayload()));
            IChatPanel panel = telegramExecutor.getPanels().get(MainMenuPanel.callback());
            if (panel instanceof MainMenuPanel mmp) {
                mmp.setPanelText("Оплата успешно зафиксирована!");
            }
            panel.execute(session);
            return;
        }
        // В конце концов просто обрабатываем так, будто это промпт
        handlePrompt(message, session);
    }

    private void handleStart(UserSession session, User user, String referralLink, String userName) {
        userService.updateUserCredentials(user, referralLink, userName);
        telegramExecutor.getPanels().get(MainMenuPanel.callback()).execute(session);
    }

    private void handlePrompt(Message message, UserSession session) {
        if (session.getChatContext().getModel() != null) {
            GenerationModel model = session.getChatContext().getModel();

            adapters.get(model).makeRequest(session);
        }
    }


}
