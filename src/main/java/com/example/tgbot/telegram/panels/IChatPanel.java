package com.example.tgbot.telegram.panels;

import com.example.tgbot.telegram.sessions.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;


public interface IChatPanel {
    void execute(UserSession session);
    // Метод нужен для инициализации коллекции всех панелей в боте
    String getLabel();
}
