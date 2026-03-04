package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class SimpleMessagePanel extends AbstractSimpleMessagePanel implements IChatPanel {

    public SimpleMessagePanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
    }


    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, session.getContextualMessage(), null, false);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.MAIN_SIMPLE_MESSAGE;
    }
}
