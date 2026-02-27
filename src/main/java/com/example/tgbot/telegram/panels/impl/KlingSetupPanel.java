package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class KlingSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public KlingSetupPanel(ObjectProvider<RegistryService> registryService, TgBot tgBot) {
        super(registryService, tgBot);
    }

    @Override
    public void execute(UserSession session) {

    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.KLING_SETUP;
    }
}
