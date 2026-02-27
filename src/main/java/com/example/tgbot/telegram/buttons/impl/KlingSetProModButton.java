package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class KlingSetProModButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;
    private final ObjectMapper mapper = new JsonMapper();
    private String mode;

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_SET_PRO_MOD;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Мультикадр");
        button.setCallbackData(getLabel().toString() + "::" + mode);
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof String s) {
                this.mode = s;
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        // Заполняем параметр в конфиге для модели
        String str;
        try {
            str = mapper.writeValueAsString("\"mode\":" + mode + ", \"");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        session.getModelsConfiguration().get(GenerationModel.KLING_3_0).setParametersFromJson(str);
        registryServiceProvider.getObject().getChatPanel(PanelType.KLING_SETUP).execute(session);
    }
}
