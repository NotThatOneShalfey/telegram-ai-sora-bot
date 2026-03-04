package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KlingMultisetSelection implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;
    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_MULTISET_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        boolean multiShots = parseMultiShots(parameters);
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(multiShots ? "Выключить мультикадр" : "Включить мультикадр");
        button.setCallbackData(getLabel().toString() + "::" + multiShots);
        return button;
    }

    private static boolean parseMultiShots(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof Boolean b) return b;
            try {
                if (o != null) return Boolean.parseBoolean(o.toString());
            } catch (IllegalArgumentException ignored) {}
        }
        return false;
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        boolean currentMultiShots = parameters.length >= 1 ? Boolean.parseBoolean(parameters[0]) : false;
        boolean newMultiShots = !currentMultiShots; // toggle
        session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0).setParametersFromJson(getJsonForOptionsChange(newMultiShots));
        panelRegistry.getChatPanel(PanelType.KLING_SETUP).execute(session);
    }

    private String getJsonForOptionsChange(boolean multiShots) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("multiShots", multiShots);
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
