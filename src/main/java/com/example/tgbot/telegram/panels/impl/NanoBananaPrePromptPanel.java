package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.buttons.ButtonType.*;

@Component
public class NanoBananaPrePromptPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public NanoBananaPrePromptPanel(ObjectProvider<RegistryService> registryServiceProvider, TgBot tgBot) {
        super(registryServiceProvider, tgBot);
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
        return PanelType.NANO_BANANA_PRE_PROMPT;
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
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
