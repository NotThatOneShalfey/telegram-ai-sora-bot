package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class AmbassadorStatsButton implements IButton {
    private final PanelRegistry panelRegistry;

    public AmbassadorStatsButton(@Lazy PanelRegistry panelRegistry) {
        this.panelRegistry = panelRegistry;
    }

    @Override
    public ButtonType getLabel() {
        return ButtonType.AMBASSADOR_STATS_CALL;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("📊 Статистика амбассадора");
        button.setCallbackData(getLabel().toString());
        return button;
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        panelRegistry.getChatPanel(PanelType.AMBASSADOR_STATS).execute(session);
    }
}
