package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class BalanceRechargePackageSelectionButton implements IButton {
    private PaidPackageEnum paidPackage;

    @Override
    public ButtonType getLabel() {
        return ButtonType.BALANCE_RECHARGE_PACKAGE_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(paidPackage.getButtonName());
        button.setCallbackData(getLabel().toString() + "::" + paidPackage.getButtonCallback());
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof PaidPackageEnum ppe) {
                this.paidPackage = ppe;
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {

    }
}
