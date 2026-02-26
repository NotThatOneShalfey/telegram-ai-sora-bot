package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TelegramExecutor;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class SimpleMessagePanel extends AbstractSimpleMessagePanel implements IChatPanel {
    @Setter
    private String panelText;

    public SimpleMessagePanel(TelegramExecutor telegramExecutor) {
        super(telegramExecutor);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), null, false);
    }

    @Override
    public String getLabel() {
        return null;
    }

    private String getText() {
        return panelText;
    }
}
