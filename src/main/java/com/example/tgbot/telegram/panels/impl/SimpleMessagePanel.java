package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class SimpleMessagePanel extends AbstractSimpleMessagePanel implements IChatPanel {
    @Setter
    private String panelText;

    public SimpleMessagePanel(RegistryService registryService, TgBot tgBot) {
        super(registryService, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), null, false);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.SIMPLE_MESSAGE;
    }

    private String getText() {
        return panelText;
    }
}
