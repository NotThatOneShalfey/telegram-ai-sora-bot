package com.example.tgbot.telegram.buttons.impl;


import com.example.tgbot.RegistryService;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.enums.AspectRatioEnum;
import com.example.tgbot.telegram.buttons.ButtonType;
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
public class AspectRatioButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;
    private AspectRatioEnum aspectRatio;
    private GenerationModel model;

    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public ButtonType getLabel() {
        return ButtonType.ASPECT_RATIO_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(aspectRatio.getButtonText());
        button.setCallbackData(getLabel().toString() + "::" + model + "::" + aspectRatio);
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof AspectRatioEnum are) {
                this.aspectRatio = are;
            } else if (o instanceof GenerationModel gm) {
                this.model = gm;
            } else {
                try {
                    this.aspectRatio = AspectRatioEnum.valueOf(o.toString());
                    this.model = GenerationModel.valueOf(o.toString());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        // Заполняем параметр в конфиге для модели
        session.getModelsConfiguration().get(model).setParametersFromJson(getJsonForOptionsChange());
        // Определяем какую панель вызывать следующую, в зависимости от модели
        PanelType nextPanel = null;
        if (model.equals(GenerationModel.NANO_BANANA_PRO)) {
            nextPanel = PanelType.NANO_BANANA_PRE_PROMPT;
        } else if (model.equals(GenerationModel.SORA_2)) {
            nextPanel = PanelType.SORA_2_SETUP;
        } else if (model.equals(GenerationModel.KLING_3_0)) {
            nextPanel = PanelType.KLING_SETUP;
        }
        try {
            registryServiceProvider.getObject().getChatPanel(nextPanel).execute(session);
        } catch (NullPointerException e) {
            log.error("AspectRatioButton: executeOnCallback ERROR -> При получении следующей панели получили NULL!");
        }
    }

    private String getJsonForOptionsChange() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("aspectRatio", aspectRatio.getValue());
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
