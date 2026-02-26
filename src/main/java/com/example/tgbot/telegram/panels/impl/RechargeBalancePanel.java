package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.buttons.PaidPackageButton;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.handlers.CallbackHandler;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelHelper;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.panels.PanelHelper.createButton;

@Component
public class RechargeBalancePanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public RechargeBalancePanel(TgBot bot) {
        super(bot);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), true);
    }

    @Override
    public String getLabel() {
        return "recharge_balance";
    }

    public static String callback() {
        return "recharge_balance";
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
        for (PaidPackageButton p : PaidPackageButton.values()) {
            String buttonName = "%d монет - %d ₽".formatted(p.getPackageAmount(), p.getPackagePrice());
            // Враппим комплексный коллбек со спецификацией, какой пакет выбрали
            String callback = CallbackHandler.wrapCallback(PaidPackageSetupPanel.callback(), p.getButtonCallback());
            rows.add(List.of(createButton(buttonName, callback)));
        }
        rows.add(List.of(createButton("Главное меню", MainMenuPanel.callback())));
        rows.add(List.of(PanelHelper.getSupportButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
