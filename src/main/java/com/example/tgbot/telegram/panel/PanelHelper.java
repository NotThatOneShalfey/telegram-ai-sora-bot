package com.example.tgbot.telegram.panel;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class PanelHelper {

    public static InlineKeyboardButton getSupportButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Поддержка");
        button.setUrl("t.me/CreativeLabAI?text=");
        return button;
    }

    private static String getQuotedBalance() {
        return """
                <blockquote>
                \uD83D\uDC8E У вас осталось: {balance} монет.
                \uD83D\uDCE9 Примеры и советы: https://t.me/sora2examples
                </blockquote>
                """;
    }

    public static void addQuotedBalanceToMessage(SendMessage sm, int balance) {
        sm.setText(sm.getText() + getQuotedBalance().replaceAll("\\{balance}", String.valueOf(balance)));
        sm.setParseMode("HTML");
        sm.setDisableWebPagePreview(true);
    }

}
