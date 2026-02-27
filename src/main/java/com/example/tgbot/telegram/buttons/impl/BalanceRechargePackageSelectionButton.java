package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.example.tgbot.telegram.panels.PanelType.AFTER_PAID_PACKAGE_SELECTED;

@Component
@RequiredArgsConstructor
public class BalanceRechargePackageSelectionButton implements IButton {
    private final ObjectProvider<RegistryService> registryService;
    private PaidPackageEnum paidPackage;

    @Override
    public ButtonType getLabel() {
        return ButtonType.BALANCE_RECHARGE_PACKAGE_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(paidPackage.getButtonName());
        button.setCallbackData(getLabel().toString() + "::" + paidPackage);
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof PaidPackageEnum ppe) {
                this.paidPackage = ppe;
            } else {
                try {
                    this.paidPackage = PaidPackageEnum.valueOf(o.toString());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        registryService.getObject().getChatPanel(AFTER_PAID_PACKAGE_SELECTED).execute(session);
    }
}
