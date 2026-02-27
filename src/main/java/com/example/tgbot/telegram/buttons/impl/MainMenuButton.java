package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class MainMenuButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;

    @Override
    public ButtonType getLabel() {
        return ButtonType.MAIN_MENU_CALL;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Главное меню");
        button.setCallbackData(getLabel().toString());
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        registryServiceProvider.getObject().getChatPanel(PanelType.MAIN_MENU).execute(session);
    }
}
