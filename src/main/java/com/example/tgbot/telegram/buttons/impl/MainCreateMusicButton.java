package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.configurations.NanoBananaOptions;
import com.example.tgbot.models.configurations.SunoOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class MainCreateMusicButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;

    @Override
    public ButtonType getLabel() {
        return ButtonType.MAIN_CREATE_MUSIC_CALL;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Создать музыку (Suno)");
        button.setCallbackData(getLabel().toString());
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        session.getModelsConfiguration().put(GenerationModel.SUNO_V5, SunoOptions.builder().build());
        registryServiceProvider.getObject().getChatPanel(PanelType.SUNO_GENRE_SELECTION).execute(session);
    }
}
