package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;

@Component
public class KlingSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {
    public KlingSetupPanel(TgBot bot) {
        super(bot);
    }

    @Override
    public void execute(UserSession session) {

    }

    @Override
    public String getLabel() {
        return "sora_setup";
    }

    public static String callback() {
        return "sora_setup";
    }
}
