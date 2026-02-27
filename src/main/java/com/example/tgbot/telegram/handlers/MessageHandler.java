package com.example.tgbot.telegram.handlers;

import com.example.tgbot.RegistryService;
import com.example.tgbot.db.User;
import com.example.tgbot.models.adapters.IRequestAdapter;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.panels.impl.MainMenuPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.tgbot.telegram.panels.PanelType.MAIN_MENU;

@Component
public class MessageHandler {

    private final ObjectProvider<RegistryService> registryServiceProvider;
    private final UserService userService;
    private final Map<GenerationModel, IRequestAdapter> adapters = new ConcurrentHashMap<>();

    public MessageHandler(UserService userService,
                          Collection<IRequestAdapter> adaptersCollection,
                          ObjectProvider<RegistryService> registryServiceProvider) {
        this.userService = userService;
        this.registryServiceProvider = registryServiceProvider;
        adaptersCollection.forEach(a -> adapters.put(a.getModel(), a));

    }


    public void handleMessage(Message message, UserSession session) {
        // Получили айди чата
        Long chatId = message.getChatId();
        User user = userService.findOrCreateUser(chatId);
        // Обработка стартового сообщения
        if (message.hasText() && message.getText().contains("/start")) {
            String referral = null;
            if (message.getText().contains(" ")) {
                referral = message.getText().split(" ")[1];
            }
            handleStart(session, user, message.getFrom().getUserName(), referral);
            return;
        }
        // Если завершение оплаты
        if (message.getSuccessfulPayment() != null) {
            userService.addBalance(user, PaidPackageEnum.getPackagePriceByName(message.getSuccessfulPayment().getInvoicePayload()));
            IChatPanel panel = registryServiceProvider.getObject().getChatPanel(MAIN_MENU);
            if (panel instanceof MainMenuPanel mmp) {
                mmp.setPanelText("Оплата успешно зафиксирована!");
            }
            panel.execute(session);
            return;
        }
        // В конце концов просто обрабатываем так, будто это промпт
        handlePrompt(message, session);
    }

    private void handleStart(UserSession session, User user, String userName, String referralLink) {
        userService.updateUserCredentials(user, userName, referralLink);
        registryServiceProvider.getObject().getChatPanel(MAIN_MENU).execute(session);
    }

    private void handlePrompt(Message message, UserSession session) {
        if (session.getChatContext().getModel() != null) {
            GenerationModel model = session.getChatContext().getModel();
            adapters.get(model).makeRequest(session);
        }
    }


}
