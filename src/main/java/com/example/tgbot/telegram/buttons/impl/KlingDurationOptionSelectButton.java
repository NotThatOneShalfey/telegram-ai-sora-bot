package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class KlingDurationOptionSelectButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;
    private String durationOption;

    @Override
    public ButtonType getLabel() {
        return ButtonType.KLING_DURATION_OPTION_SELECT;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(durationOption + " секунд");
        button.setCallbackData(getLabel().toString() + "::" + durationOption);
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof String s) {
                this.durationOption = s;
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        registryServiceProvider.getObject().getChatPanel(PanelType.KLING_SETUP).execute(session);
    }
}
