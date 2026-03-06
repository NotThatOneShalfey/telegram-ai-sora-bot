package com.example.tgbot.telegram.button.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.button.IButton;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.example.tgbot.telegram.button.ButtonType.KLING_DURATION_SELECTION;

@Component
@RequiredArgsConstructor
public class KlingDurationSelectionButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;
    @Override
    public ButtonType getLabel() {
        return KLING_DURATION_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Длительность");
        button.setCallbackData(getLabel().toString());
        return button;
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        panelRegistry.getChatPanel(PanelType.KLING_DURATION_SELECTION).execute(session);
    }
}
