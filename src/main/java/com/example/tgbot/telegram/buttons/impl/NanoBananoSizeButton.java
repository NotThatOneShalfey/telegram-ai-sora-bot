package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.enums.NanoBananoSize;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class NanoBananoSizeButton implements IButton {
    private NanoBananoSize size;

    @Override
    public ButtonType getLabel() {
        return ButtonType.NANO_BANANO_SIZE_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(size.getButtonText());
        button.setCallbackData(getLabel() + "::" + size.getValue());
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof NanoBananoSize nbs) {
                this.size = nbs;
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {

    }
}
