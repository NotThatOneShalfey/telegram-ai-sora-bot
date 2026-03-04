package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelHelper;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.buttons.ButtonType.*;

@Component
public class MainMenuPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    public MainMenuPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
    }


    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(session), getKeyboard(session), true);
    }


    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    // Статический метод нужен для ссылки кнопок на следующую панель
    public static PanelType getStaticLabel() {
        return PanelType.MAIN_MENU;
    }

    private String getText(UserSession session) {
        String contextualMessage = session.getChatContext().getContextualMessage();
        if (contextualMessage != null && !contextualMessage.isEmpty()) {
            session.getChatContext().setContextualMessage(null); // consume after use
            return contextualMessage;
        }
        return """
            🚀 Добро пожаловать в CreatorLabAI
                      
            Здесь ты можешь:
            🖼 Создать изображение (NanoBanana Pro). Обложки, аватары, иллюстрации, сцены — за один запрос.
                      
            🎬 Создать видео по тексту (Sora 2). Опиши идею — получи готовый ролик.
                      
            🎥 Оживить изображение (Kling 3.0 или Sora 2). Преврати картинку в динамичное видео.
                      
            🎵 Создать свою музыку (Suno). Опиши идею, укажи стиль и стань творцом музыки.
                      
            Выбирай инструмент ниже и начинай создавать 👇
          """;
    }

    private InlineKeyboardMarkup getKeyboard(UserSession us) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(MAIN_CREATE_IMAGE_CALL).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_CREATE_VIDEO_CALL).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_CREATE_MUSIC_CALL).getKeyboardButton()));
        // Проверяем, нужно ли создавать подарок
        boolean addGift = us.getUser().getLinkUsed() == null && !us.getUser().isBonusReceived();
        if (addGift) {
            rows.add(List.of(super.getButton(GET_GIFT_CALL).getKeyboardButton()));
        }
        rows.add(List.of(PanelHelper.getSupportButton(), super.getButton(RECHARGE_BALANCE_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
