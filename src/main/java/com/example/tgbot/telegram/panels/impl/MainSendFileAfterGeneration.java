package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.beans.factory.ObjectProvider;

public class MainSendFileAfterGeneration extends AbstractSimpleMessagePanel implements IChatPanel {

    public MainSendFileAfterGeneration(ObjectProvider<RegistryService> registryServiceProvider, TgBot tgBot) {
        super(registryServiceProvider, tgBot);
    }

    @Override
    public void execute(UserSession session) {
    }

    @Override
    public PanelType getLabel() {
        return PanelType.MAIN_SEND_READY_FILE;
    }
}
