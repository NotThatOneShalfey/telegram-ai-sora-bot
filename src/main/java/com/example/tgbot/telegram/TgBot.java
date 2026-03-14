package com.example.tgbot.telegram;

import com.example.tgbot.domain.model.User;
import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.dto.api.InterfaceDTORequest;
import com.example.tgbot.dto.api.SubmitOutcome;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.handler.CallbackHandler;
import com.example.tgbot.telegram.handler.InterfaceCallHandler;
import com.example.tgbot.telegram.handler.InvoiceHandler;
import com.example.tgbot.telegram.handler.MessageHandler;
import com.example.tgbot.telegram.session.UserSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

    @Getter
    @Value("${common.allowed-dev-user-ids}")
    private String[] allowedUserIdsOnDev;

    private final List<Long> allowedUserIdsList;

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
        allowedUserIdsList = allowedUserIdsOnDev != null
                ? Arrays.stream(allowedUserIdsOnDev).map(Long::valueOf).collect(Collectors.toList())
                : new ArrayList<>();
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

    /** Синхронная обработка web-запроса, возвращает SubmitOutcome (успех или код ошибки). */
    public SubmitOutcome processWebInterfaceRequestSync(InterfaceDTORequest request) {
        try {
            return processWebInterfaceReq(request);
        } catch (Exception e) {
            log.error("Error processing web interface request", e);
            return SubmitOutcome.fail(ErrorCode.E008);
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
                if (devBuild && allowedUserIdsList.contains(user.getId())) {
                    return;
                }
                UserSession userSession = sessions.computeIfAbsent(update.getCallbackQuery().getMessage().getChatId().toString(), k -> new UserSession(user));
                userSession.touch();
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
                if (devBuild && allowedUserIdsList.contains(user.getId())) {
                    return;
                }
                UserSession userSession = sessions.computeIfAbsent(update.getMessage().getChatId().toString(), k -> new UserSession(user));
                userSession.touch();
                messageHandler.handleMessage(update.getMessage(), userSession);
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

    /** Удаляет сессии, не активные более заданного времени. */
    public int cleanSessionsOlderThan(LocalDateTime cutoff) {
        int removed = 0;
        Iterator<Map.Entry<String, UserSession>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UserSession> e = it.next();
            LocalDateTime last = e.getValue().getLastActionDateTime();
            if (last != null && last.isBefore(cutoff)) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.info("Cleaned {} stale user sessions", removed);
        }
        return removed;
    }

    private SubmitOutcome processWebInterfaceReq(InterfaceDTORequest request) {
        log.trace("Call processWebInterfaceReq with request={}", request);
        User user = userService.findOrCreateUser(request.getUserId());
        if (devBuild && allowedUserIdsList.contains(user.getId())) {
            return null;
        }
        UserSession userSession = sessions.computeIfAbsent(String.valueOf(user.getTelegramId()), k -> new UserSession(user));
        userSession.touch();
        return interfaceCallHandler.handleRequest(userSession, request.getOptionsBody(), request.getModel());
    }
}
