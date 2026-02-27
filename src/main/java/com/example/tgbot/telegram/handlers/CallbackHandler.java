package com.example.tgbot.telegram.handlers;

import com.example.tgbot.RegistryService;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackHandler {

    private final RegistryService registryService;
    private final UserService userService;

    public static String wrapCallback(String panelToShow, String additionalOption) {
        // Коллбэк будет вида PANEL::CUSTOM=XXX
        String sb = panelToShow +
                "::" + additionalOption;
        return sb;
    }

    private void unwrapCallback(String callbackData) {
        if (callbackData.contains("::")) {
            String[] callbackArray = callbackData.split("::");
        }
    }

    public void handleCallback(CallbackQuery cq, UserSession userSession) {
        log.trace("handleCallback, Message={}", cq.getMessage());
    }

}
