package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;

@Component
public class SoraFormatSelectionPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public SoraFormatSelectionPanel(RegistryService registryService, TgBot tgBot) {
        super(registryService, tgBot);
    }

    @Override
    public void execute(UserSession session) {

    }

    @Override
    public PanelType getLabel() {
        return PanelType.SORA_2_FORMAT_SELECTION;
    }
}
