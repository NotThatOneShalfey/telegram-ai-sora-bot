package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class NanoBananaSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    @Override
    public void execute(UserSession session) {

    }

    @Override
    public String getLabel() {
        return "gen_image_after_format_selection";
    }

    public static String callback() {
        return "gen_image_after_format_selection";
    }

    private String getText() {
        String text = """
                🖼 Nano Banana Pro — генерация изображений
                                
                Отправь:
                                
                ✍️ Текстовый промпт — и я создам изображение с нуля
                или
                🖼 Промпт + картинку — чтобы изменить или доработать загруженное изображение
                                
                Просто отправь описание одним сообщением.
                Если хочешь изменить конкретную картинку — прикрепи её вместе с текстом.
                                
                💸 СТОИМОСТЬ: 20 монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                """;
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        return null;
    }
}
