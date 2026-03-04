package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.registry.ButtonRegistry;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelHelper;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class RechargeBalancePanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public RechargeBalancePanel(ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.MAIN_RECHARGE_BALANCE;
    }

    private String getText() {
        String text = """
                💳 Пополнение баланса
                                
                1 монета = 1 ₽
                                
                Монеты используются для генерации:
                🎬 видео
                🖼 изображений
                🎥 анимации изображений
                                
                Выбери подходящий пакет ниже 👇
                                
                💡 Чем больше пакет — тем выгоднее и удобнее для активной работы.
                """;
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (PaidPackageEnum p : PaidPackageEnum.values()) {
            rows.add(List.of(super.getButton(ButtonType.BALANCE_RECHARGE_PACKAGE_SELECTION).getKeyboardButton(p)));
        }
        rows.add(List.of(super.getButton(ButtonType.MAIN_MENU_CALL).getKeyboardButton()));
        rows.add(List.of(PanelHelper.getSupportButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
