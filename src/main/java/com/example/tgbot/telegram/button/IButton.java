package com.example.tgbot.telegram.button;

import com.example.tgbot.telegram.session.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public interface IButton {
    ButtonType getLabel();

    /** Creates keyboard button with given parameters (for building UI). Parameters order must match callback data format. */
    InlineKeyboardButton getKeyboardButton(Object... parameters);

    /** Handles callback execution. parameters are parsed from callback data (excluding button type). */
    void executeOnCallback(UserSession session, String[] parameters);
}
