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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


@Component
@RequiredArgsConstructor
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
        button.setCallbackData(getLabel().toString() + "::" + model.getRequestModelName() + "::" + aspectRatio.getValue());
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof AspectRatioEnum ao) {
                this.aspectRatio = ao;
            } else if (o instanceof GenerationModel bm) {
                this.model = bm;
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        // Заполняем параметр в конфиге для модели
        String str;
        try {
            str = mapper.writeValueAsString("\"aspectRatio\":" + aspectRatio.getValue());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        session.getModelsConfiguration().get(model).setParametersFromJson(str);

        // Определяем какую панель вызывать следующую, в зависимости от модели
        PanelType nextPanel = null;
        if (model.equals(GenerationModel.NANO_BANANA_PRO)) {
            nextPanel = PanelType.NANO_BANANA_PRE_PROMPT;
        } else if (model.equals(GenerationModel.SORA_2)) {
            nextPanel = PanelType.SORA_2_SETUP;
        } else if (model.equals(GenerationModel.KLING_3_0)) {
            nextPanel = PanelType.KLING_SETUP;
        }
        registryServiceProvider.getObject().getChatPanel(nextPanel).execute(session);
    }


}
