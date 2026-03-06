package com.example.tgbot.telegram.button.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.button.IButton;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
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
public class KlingSoundSelectionButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;
    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_SOUND_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        boolean withSound = parseWithSound(parameters);
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(withSound ? "Выключить звуки" : "Включить звуки");
        button.setCallbackData(getLabel().toString() + "::" + withSound);
        return button;
    }

    private static boolean parseWithSound(Object... parameters) {
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
        boolean currentWithSound = parameters.length >= 1 ? Boolean.parseBoolean(parameters[0]) : false;
        boolean newWithSound = !currentWithSound; // toggle
        session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0).setParametersFromJson(getJsonForOptionsChange(newWithSound));
        panelRegistry.getChatPanel(PanelType.KLING_SETUP).execute(session);
    }

    private String getJsonForOptionsChange(boolean withSound) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("withSound", withSound);
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
