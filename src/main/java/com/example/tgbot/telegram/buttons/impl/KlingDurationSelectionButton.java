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

import static com.example.tgbot.telegram.buttons.ButtonType.KLING_DURATION_SELECTION;
import static com.example.tgbot.telegram.buttons.ButtonType.SORA_2_BACK_TO_MODEL_SELECTION;

@Component
@RequiredArgsConstructor
public class KlingDurationSelectionButton implements IButton {
    private final ObjectProvider<RegistryService> registryServiceProvider;
    @Override
    public ButtonType getLabel() {
        return KLING_DURATION_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Длительность");
        button.setCallbackData(getLabel().toString());
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        registryServiceProvider.getObject().getChatPanel(PanelType.KLING_DURATION_SELECTION).execute(session);
    }
}
