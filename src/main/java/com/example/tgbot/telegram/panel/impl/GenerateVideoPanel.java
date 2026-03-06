package com.example.tgbot.telegram.panel.impl;

import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.panel.IChatPanel;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class GenerateVideoPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public GenerateVideoPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.MAIN_GENERATE_VIDEO;
    }

    public String getText() {
        String text = """
                \uD83C\uDFA5 Генерация видео по изображению
                Выберите модель:
                \uD83D\uDE80 Kling 3.0 — 150 Монет
                • Можно анимировать живых людей
                • Меньше цензуры
                • Более динамичные сцены
                \uD83D\uDC49 Подходит для реалистичных и «живых» видео
                
                \uD83C\uDFAC Sora 2 — 100 Монет
                • Более строгая модерация
                • Нельзя генерировать реальных людей
                • Отличная озвучка на русском
                \uD83D\uDC49 Лучше для мультфильмов и обучающих видео
                
                1  монета = 1 рубль
                Выберите модель ниже \uD83D\uDC47
                """;
        return text;
    }

    public InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(ButtonType.KLING_MODEL_SELECTED).getKeyboardButton()));
        rows.add(List.of(super.getButton(ButtonType.SORA_2_MODEL_SELECTED).getKeyboardButton()));
        rows.add(List.of(super.getButton(ButtonType.MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
