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
public class KlingSoundSelectionButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;
    private final ObjectMapper mapper = new JsonMapper();
    private Boolean buttonOn;

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_SOUND_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Включить звуки");
        button.setCallbackData(getLabel().toString() + "::" + buttonOn);
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof Boolean b) {
                this.buttonOn = b;
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        // Заполняем параметр в конфиге для модели
        String str;
        try {
            str = mapper.writeValueAsString("\"withSound\":" + buttonOn);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        session.getModelsConfiguration().get(GenerationModel.KLING_3_0).setParametersFromJson(str);
        registryServiceProvider.getObject().getChatPanel(PanelType.KLING_SETUP).execute(session);
    }

}
