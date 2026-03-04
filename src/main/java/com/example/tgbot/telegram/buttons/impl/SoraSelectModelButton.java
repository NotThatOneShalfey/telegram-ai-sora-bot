package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class SoraSelectModelButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;

    @Override
    public ButtonType getLabel() {
        return ButtonType.SORA_2_MODEL_SELECTED;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Sora 2");
        button.setCallbackData(getLabel().toString());
        return button;
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        panelRegistry.getChatPanel(PanelType.SORA_2_SETUP).execute(session);
    }
}
