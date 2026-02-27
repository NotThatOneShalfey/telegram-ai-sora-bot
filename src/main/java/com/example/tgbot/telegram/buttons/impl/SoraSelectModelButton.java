package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.configurations.SoraOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class SoraSelectModelButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;

    @Override
    public ButtonType getLabel() {
        return ButtonType.SORA_2_MODEL_SELECTED;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Sora 2");
        button.setCallbackData(getLabel().toString());
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        session.getModelsConfiguration().put(GenerationModel.SORA_2, SoraOptions.builder().build());
        registryServiceProvider.getObject().getChatPanel(PanelType.SORA_2_SETUP).execute(session);
    }
}
