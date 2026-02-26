package com.example.tgbot.telegram.handlers;

import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.TgBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final TgBot tgBot;
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

    public void handleCallback(CallbackQuery cq) {
        cq.getMessage().getChatId();
        userService.findUser();
        tgBot.getSessions().get();


    }

}
