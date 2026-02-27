package com.example.tgbot.telegram.panels;

import com.example.tgbot.telegram.sessions.UserSession;


public interface IChatPanel {
    void execute(UserSession session);
    // Метод нужен для инициализации коллекции всех панелей в боте
    PanelType getLabel();
}
