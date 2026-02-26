package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;

@Component
public class NanoBananaAfterFormatPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    public NanoBananaAfterFormatPanel(TgBot bot) {
        super(bot);
    }

    @Override
    public void execute(UserSession session) {

    }

    @Override
    public String getLabel() {
        return null;
    }

    public static String callback() {
        return null;
    }
}
