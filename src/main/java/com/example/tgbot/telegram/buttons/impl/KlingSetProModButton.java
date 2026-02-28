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

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KlingSetProModButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;
    private final ObjectMapper mapper = new JsonMapper();
    private Boolean proMode;

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_SET_PRO_MOD;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(proMode ? "Перейти на Стандартный режим" : "Перейти на Pro режим");
        button.setCallbackData(getLabel().toString() + "::" + proMode);
        return button;
    }

    // Здесь очень важно!!!
    // Это одна кнопка с переключалкой, поэтому при получении старого параметра, мы инверсируем значение
    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof Boolean b) {
                this.proMode = !b;
            } else {
                try {
                    this.proMode = !Boolean.parseBoolean(o.toString());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        // Заполняем параметр в конфиге для модели
        session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0).setParametersFromJson(getJsonForOptionsChange());
        registryServiceProvider.getObject().getChatPanel(PanelType.KLING_SETUP).execute(session);
    }

    private String getJsonForOptionsChange() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("mode", proMode ? "pro" : "std");
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
