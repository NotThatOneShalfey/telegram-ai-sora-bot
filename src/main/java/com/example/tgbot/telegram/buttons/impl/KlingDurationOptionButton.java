package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.enums.VideoDurationEnum;
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
@Slf4j
public class KlingDurationOptionButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;
    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_DURATION_OPTION_SELECT;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        VideoDurationEnum duration = parseDuration(parameters);
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(duration.getButtonText());
        button.setCallbackData(getLabel().toString() + "::" + duration);
        return button;
    }

    private static VideoDurationEnum parseDuration(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof VideoDurationEnum vde) return vde;
            try {
                if (o != null) return VideoDurationEnum.valueOf(o.toString());
            } catch (IllegalArgumentException ignored) {}
        }
        return VideoDurationEnum.DURATION_10;
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        VideoDurationEnum duration = parameters.length >= 1 ? VideoDurationEnum.valueOf(parameters[0]) : VideoDurationEnum.DURATION_10;
        session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0).setParametersFromJson(getJsonForOptionsChange(duration));
        try {
            panelRegistry.getChatPanel(PanelType.KLING_SETUP).execute(session);
        } catch (NullPointerException e) {
            log.error("AspectRatioButton: executeOnCallback ERROR -> При получении следующей панели получили NULL!");
        }
    }

    private String getJsonForOptionsChange(VideoDurationEnum duration) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("duration", duration.getValue());
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
