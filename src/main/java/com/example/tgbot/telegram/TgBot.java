package com.example.tgbot.telegram;

import com.example.tgbot.db.User;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.configurations.dto.InterfaceDTORequest;
import com.example.tgbot.models.configurations.dto.WebSubmitResult;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.handlers.CallbackHandler;
import com.example.tgbot.telegram.handlers.InterfaceCallHandler;
import com.example.tgbot.telegram.handlers.InvoiceHandler;
import com.example.tgbot.telegram.handlers.MessageHandler;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;
import java.util.concurrent.*;

@Component
@Slf4j
public class TgBot extends TelegramWebhookBot {
    private final Executor taskExecutor;
    // Сессии юзеров с ключом по ChatId
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
    private final CallbackHandler callbackHandler;
    private final MessageHandler messageHandler;
    private final InvoiceHandler invoiceHandler;
    private final UserService userService;
    private final InterfaceCallHandler interfaceCallHandler;

    @Getter
    @Value("${telegram.bot.payment-token:}")
    private String providerToken;

    @Getter
    @Value("${common.dev-build:}")
    private Boolean devBuild;

    // Инициализация бота с дефолт параметрами, botToken из .env
    public TgBot(@Value("${telegram.bot.token}") String botToken,
                 CallbackHandler callbackHandler,
                 MessageHandler messageHandler,
                 InvoiceHandler invoiceHandler,
                 InterfaceCallHandler interfaceCallHandler,
                 UserService userService,
                 @Qualifier("botExecutor") Executor taskExecutor) {
        super(botToken);
        this.callbackHandler = callbackHandler;
        this.messageHandler = messageHandler;
        this.invoiceHandler = invoiceHandler;
        this.taskExecutor = taskExecutor;
        this.userService = userService;
        this.interfaceCallHandler = interfaceCallHandler;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        taskExecutor.execute(() -> {
            try {
                processUpdate(update);
            } catch (Exception e) {
                log.error("Unhandled exception while processing update", e);
            }
        });
        return null;
    }

    public void onWebInterfaceRequest(InterfaceDTORequest request) {
        taskExecutor.execute(() -> {
            try {
                processWebInterfaceReq(request);
            } catch (Exception e) {
                log.error("Unhandled exception while processing update", e);
            }
        });
    }

    /** Синхронная обработка web-запроса, возвращает taskId и баланс при успешной постановке задачи. */
    public Optional<WebSubmitResult> processWebInterfaceRequestSync(InterfaceDTORequest request) {
        try {
            return processWebInterfaceReq(request);
        } catch (Exception e) {
            log.error("Error processing web interface request", e);
            return Optional.empty();
        }
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
                User user = userService.findOrCreateUser(update.getCallbackQuery().getMessage().getChatId());
                // Проверка на дев билд
                if (devBuild && (user.getId() != 1 && user.getId() != 2 && user.getId() != 4)) {
                    return;
                }
                UserSession userSession = sessions.computeIfAbsent(update.getCallbackQuery().getMessage().getChatId().toString(), k -> new UserSession(user));
                callbackHandler.handleCallback(update.getCallbackQuery(), userSession);
                return;
            }
            // Если подтверждение оплаты
            if (update.hasPreCheckoutQuery()) {
                log.trace("update has preCheckoutQuery");
                invoiceHandler.handlePreCheckoutQuery(update.getPreCheckoutQuery().getId(), this);
                return;
            }
            // Если не callback и не оплата, то заходим в обработку сообщения
            if (update.hasMessage()) {
                log.trace("update has Message");
                User user = userService.findOrCreateUser(update.getMessage().getChatId());
                // Проверка на дев билд
                if (devBuild && (user.getId() != 1 && user.getId() != 2 && user.getId() != 4)) {
                    return;
                }
                UserSession userSession = sessions.computeIfAbsent(update.getMessage().getChatId().toString(), k -> new UserSession(user));
                messageHandler.handleMessage(update.getMessage(), userSession);
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

    public String processExampleRequest(String userName, GenerationModel model) {
        log.trace("Call processExampleRequest with UserName={}", userName);
        User user = userService.findUserByUserName(userName);
        UserSession userSession = sessions.computeIfAbsent(String.valueOf(user.getTelegramId()), k -> new UserSession(user));
        IModelRequestOptions requestOptions = userSession.getCurrentRequestOptionsByModel(model);
        return requestOptions.convertToDTO();
    }

    private Optional<WebSubmitResult> processWebInterfaceReq(InterfaceDTORequest request) {
        log.trace("Call processWebInterfaceReq with request={}", request);
        User user = userService.findOrCreateUser(request.getUserId());
        UserSession userSession = sessions.computeIfAbsent(String.valueOf(user.getTelegramId()), k -> new UserSession(user));
        return interfaceCallHandler.handleRequest(userSession, request.getOptionsBody(), request.getModel());
    }
}
