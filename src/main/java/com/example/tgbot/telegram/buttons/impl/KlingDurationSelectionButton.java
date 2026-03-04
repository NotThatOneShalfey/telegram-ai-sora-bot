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

import static com.example.tgbot.telegram.buttons.ButtonType.KLING_DURATION_SELECTION;
import static com.example.tgbot.telegram.buttons.ButtonType.SORA_2_BACK_TO_MODEL_SELECTION;

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
