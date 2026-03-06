package com.example.tgbot.telegram.button.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.button.IButton;
import com.example.tgbot.telegram.button.enums.PaidPackageEnum;
import com.example.tgbot.telegram.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.example.tgbot.telegram.panel.PanelType.MAIN_AFTER_PAID_PACKAGE_SELECTED;

@Component
@RequiredArgsConstructor
public class BalanceRechargePackageSelectionButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;

    @Override
    public ButtonType getLabel() {
        return ButtonType.BALANCE_RECHARGE_PACKAGE_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        PaidPackageEnum paidPackage = parsePackage(parameters);
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(paidPackage.getButtonName());
        button.setCallbackData(getLabel().toString() + "::" + paidPackage);
        return button;
    }

    private static PaidPackageEnum parsePackage(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof PaidPackageEnum ppe) return ppe;
            try {
                if (o != null) return PaidPackageEnum.valueOf(o.toString());
            } catch (IllegalArgumentException ignored) {}
        }
        throw new IllegalArgumentException("PaidPackageEnum required");
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        PaidPackageEnum paidPackage = parameters.length >= 1 ? PaidPackageEnum.valueOf(parameters[0]) : PaidPackageEnum.PACKAGE_100;
        session.setPaymentInfo(paidPackage);
        panelRegistry.getChatPanel(MAIN_AFTER_PAID_PACKAGE_SELECTED).execute(session);
    }
}
