package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class SimpleMessagePanel extends AbstractSimpleMessagePanel implements IChatPanel {
    @Setter
    private String panelText;

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
