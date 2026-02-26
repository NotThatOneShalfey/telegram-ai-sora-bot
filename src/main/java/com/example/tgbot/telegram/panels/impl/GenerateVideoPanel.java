package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.panels.PanelHelper.createButton;

@Component
public class GenerateVideoPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    // Статический метод нужен для ссылки кнопок на следующую панель
    public static String callback() {
        return "generate_video";
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public String getLabel() {
        return "generate_video";
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
        rows.add(List.of(createButton("Kling 3.0", KlingSetupPanel.callback())));
        rows.add(List.of(createButton("Sora 2", SoraSetupPanel.callback())));
        rows.add(List.of(createButton("Главное меню", MainMenuPanel.callback())));
        markup.setKeyboard(rows);
        return markup;
    }
}
