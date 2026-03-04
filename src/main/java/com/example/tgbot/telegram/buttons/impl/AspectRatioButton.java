package com.example.tgbot.telegram.buttons.impl;


import com.example.tgbot.registry.PanelRegistry;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
@Slf4j
public class AspectRatioButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;
    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public ButtonType getLabel() {
        return ButtonType.ASPECT_RATIO_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        AspectRatioEnum aspectRatio = parseAspectRatio(parameters);
        GenerationModel model = parseModel(parameters);
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(aspectRatio.getButtonText());
        button.setCallbackData(getLabel().toString() + "::" + model + "::" + aspectRatio);
        return button;
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        GenerationModel model = parameters.length >= 1 ? GenerationModel.valueOf(parameters[0]) : GenerationModel.SORA_2;
        AspectRatioEnum aspectRatio = parameters.length >= 2 ? AspectRatioEnum.valueOf(parameters[1]) : AspectRatioEnum.FORMAT_9_16;

        session.getCurrentRequestOptionsByModel(model).setParametersFromJson(getJsonForOptionsChange(aspectRatio));

        PanelType nextPanel = switch (model) {
            case NANO_BANANA_PRO -> PanelType.NANO_BANANA_SETUP;
            case SORA_2, SORA_2_WITH_IMAGE -> PanelType.SORA_2_SETUP;
            case KLING_3_0 -> PanelType.KLING_SETUP;
            default -> PanelType.MAIN_MENU;
        };
        try {
            panelRegistry.getChatPanel(nextPanel).execute(session);
        } catch (NullPointerException e) {
            log.error("AspectRatioButton: executeOnCallback ERROR -> При получении следующей панели получили NULL!");
        }
    }

    private static AspectRatioEnum parseAspectRatio(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof AspectRatioEnum are) return are;
            try {
                if (o != null) return AspectRatioEnum.valueOf(o.toString());
            } catch (IllegalArgumentException ignored) {}
        }
        return AspectRatioEnum.FORMAT_9_16;
    }

    private static GenerationModel parseModel(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof GenerationModel gm) return gm;
            try {
                if (o != null) return GenerationModel.valueOf(o.toString());
            } catch (IllegalArgumentException ignored) {}
        }
        return GenerationModel.SORA_2;
    }

    private String getJsonForOptionsChange(AspectRatioEnum aspectRatio) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("aspectRatio", aspectRatio.getValue());
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
