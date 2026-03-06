package com.example.tgbot.telegram.panel.impl;

import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panel.IChatPanel;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
public class MainSendFileAfterGeneration extends AbstractSimpleMessagePanel implements IChatPanel {

    public MainSendFileAfterGeneration(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
    }

    @Override
    public void execute(UserSession session) {
    }

    @Override
    public PanelType getLabel() {
        return PanelType.MAIN_SEND_READY_FILE;
    }
}
