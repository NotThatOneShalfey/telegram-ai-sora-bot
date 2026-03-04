package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class MainCreateMusicButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;

    @Override
    public ButtonType getLabel() {
        return ButtonType.MAIN_CREATE_MUSIC_CALL;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Создать музыку");
        button.setCallbackData(getLabel().toString());
        return button;
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        panelRegistry.getChatPanel(PanelType.SUNO_GENRE_SELECTION).execute(session);
    }
}
