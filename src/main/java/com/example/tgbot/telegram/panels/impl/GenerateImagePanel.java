package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.buttons.AspectRatioButton;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.handlers.CallbackHandler;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.panels.PanelHelper.createButton;

@Component
public class GenerateImagePanel extends AbstractSimpleMessagePanel implements IChatPanel {

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public String getLabel() {
        return "generate_image";
    }

    public static String callback() {
        return "generate_image";
    }

    public String getText() {
        String text = """
                📽️Выберите удобный формат📽️
                """;
        return text;
    }

    public InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(createButton(AspectRatioButton.FORMAT_16_9.getButtonText(), CallbackHandler.wrapCallback(NanoBananaAfterFormatPanel.callback(), AspectRatioButton.FORMAT_16_9.getButtonCallback()))
                , createButton(AspectRatioButton.FORMAT_9_16.getButtonText(), CallbackHandler.wrapCallback(NanoBananaAfterFormatPanel.callback(), AspectRatioButton.FORMAT_9_16.getButtonText()))));
        rows.add(List.of(createButton("Главное меню", MainMenuPanel.callback())));
        markup.setKeyboard(rows);
        return markup;
    }
}
