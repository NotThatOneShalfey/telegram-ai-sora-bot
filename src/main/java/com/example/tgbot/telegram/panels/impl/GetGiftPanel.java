package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TelegramExecutor;
import com.example.tgbot.telegram.TgBot;
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
public class GetGiftPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    public GetGiftPanel(TelegramExecutor telegramExecutor) {
        super(telegramExecutor);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), true);
    }

    @Override
    public String getLabel() {
        return "gift";
    }

    public static String callback() {
        return "gift";
    }

    private String getText() {
        String text = """
                \uD83C\uDF81 Поздравляем!
                
                Ты получил 100 монет!✨
                1 Монета = 1 Рублю
                Теперь ты можешь творить!
                """;
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(createButton("Создать изображение (Nano Banana Pro)", GenerateImagePanel.callback())));
        rows.add(List.of(createButton("Создать видео", GenerateVideoPanel.callback())));
        rows.add(List.of(createButton("Создать музыку (Suno)", GenerateMusicPanel.callback())));
        rows.add(List.of(PanelHelper.getSupportButton(), createButton("Пополнить баланс", RechargeBalancePanel.callback())));
        markup.setKeyboard(rows);
        return markup;
    }
}
