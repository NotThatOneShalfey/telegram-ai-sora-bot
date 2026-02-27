package com.example.tgbot.telegram.buttons;

import com.example.tgbot.telegram.sessions.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public interface IButton {
    ButtonType getLabel();
    InlineKeyboardButton getKeyboardButton();
    IButton setParameters(Object... parameters);
    void executeOnCallback(UserSession session);
}
