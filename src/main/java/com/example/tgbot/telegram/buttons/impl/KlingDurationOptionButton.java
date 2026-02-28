package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.RegistryService;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KlingDurationOptionButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;
    private final ObjectMapper mapper = new JsonMapper();
    private VideoDurationEnum duration = VideoDurationEnum.DURATION_10;

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_DURATION_OPTION_SELECT;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(duration.getButtonText());
        button.setCallbackData(getLabel().toString() + "::" + duration);
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof VideoDurationEnum vde) {
                this.duration = vde;
            } else {
                try {
                    this.duration = VideoDurationEnum.valueOf(o.toString());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        // Заполняем параметр в конфиге для модели
        session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0).setParametersFromJson(getJsonForOptionsChange());
        try {
            registryServiceProvider.getObject().getChatPanel(PanelType.KLING_SETUP).execute(session);
        } catch (NullPointerException e) {
            log.error("AspectRatioButton: executeOnCallback ERROR -> При получении следующей панели получили NULL!");
        }
    }

    private String getJsonForOptionsChange() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("duration", duration.getValue());
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
