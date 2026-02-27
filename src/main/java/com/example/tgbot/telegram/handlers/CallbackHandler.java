package com.example.tgbot.telegram.handlers;

import com.example.tgbot.RegistryService;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackHandler {

    private final ObjectProvider<RegistryService> registryServiceProvider;

    public void handleCallback(CallbackQuery cq, UserSession userSession) {
        log.trace("call handleCallback ---> CallbackData={}", cq.getData());
        String[] dataArray = cq.getData().split("::");
        String buttonType = dataArray[0];
        IButton button = registryServiceProvider.getObject().getButton(ButtonType.valueOf(buttonType));
        if (dataArray.length > 1) {
            for (int i=1; i<dataArray.length; i++) {
                button.setParameters(dataArray[i]);
            }
        }
        button.executeOnCallback(userSession);
    }

}
