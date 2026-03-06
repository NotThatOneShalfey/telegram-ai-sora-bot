package com.example.tgbot.telegram.panel;

import com.example.tgbot.telegram.session.UserSession;


public interface IChatPanel {
    void execute(UserSession session);
    // Метод нужен для инициализации коллекции всех панелей в боте
    PanelType getLabel();
}
