package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KlingSetProModButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;
    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_SET_PRO_MOD;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        boolean currentProMode = parameters.length >= 1 && Boolean.parseBoolean(String.valueOf(parameters[0]));
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(currentProMode ? "Перейти на Стандартный режим" : "Перейти на Pro режим");
        button.setCallbackData(getLabel().toString() + "::" + currentProMode);
        return button;
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        boolean currentProMode = parameters.length >= 1 ? Boolean.parseBoolean(parameters[0]) : false;
        boolean newProMode = !currentProMode; // toggle
        session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0).setParametersFromJson(getJsonForOptionsChange(newProMode));
        panelRegistry.getChatPanel(PanelType.KLING_SETUP).execute(session);
    }

    private String getJsonForOptionsChange(boolean proMode) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("mode", proMode ? "pro" : "std");
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
