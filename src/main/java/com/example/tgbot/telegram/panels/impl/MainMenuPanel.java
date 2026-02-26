package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelHelper;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.panels.PanelHelper.createButton;

@Component
public class MainMenuPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    @Setter
    private String panelText = "";

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(session), true);
    }


    @Override
    public String getLabel() {
        return "main_menu";
    }

    // Статический метод нужен для ссылки кнопок на следующую панель
    public static String callback() {
        return "main_menu";
    }

    public String getText() {
        String text = """
              \uD83D\uDE80 Добро пожаловать в CreatorLabAI
                
                Твоя AI-студия для создания контента прямо в Telegram.
                
                                Здесь ты можешь:
                
            \uD83D\uDDBC Создать изображение (Nano Banana Pro)
                Обложки, аватары, иллюстрации, сцены — за один запрос.
                
               \uD83C\uDFAC Создать видео по тексту (Sora 2)
                      Опиши идею — получи готовый ролик.
                        
          \uD83C\uDFA5 Оживить изображение (Kling 3.0 или Sora 2)
                        Преврати картинку в динамичное видео.
                        
                \uD83C\uDFB5 Создать свою музыку (Suno)
                   Опиши идею, укажи стиль и стань творцом музыки.
               
                Выбирай инструмент ниже и начинай создавать \uD83D\uDC47
          """;
        return panelText.isEmpty() ? text : panelText;
    }

    private InlineKeyboardMarkup getKeyboard(UserSession us) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(createButton("Создать изображение (Nano Banana Pro)", GenerateImagePanel.callback())));
        rows.add(List.of(createButton("Создать видео", GenerateVideoPanel.callback())));
        rows.add(List.of(createButton("Создать музыку (Suno)", GenerateMusicPanel.callback())));
        // Проверяем, нужно ли создавать подарок
        boolean addGift = us.getUser().getLinkUsed() == null && !us.getUser().isBonusReceived();
        if (addGift) {
            rows.add(List.of(createButton("\uD83C\uDF81 Получить подарок \uD83C\uDF81", GetGiftPanel.callback())));
        }
        rows.add(List.of(PanelHelper.getSupportButton(), createButton("Пополнить баланс", RechargeBalancePanel.callback())));
        markup.setKeyboard(rows);
        return markup;
    }
}
