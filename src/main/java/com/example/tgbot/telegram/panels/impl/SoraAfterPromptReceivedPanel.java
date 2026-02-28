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
public class SoraAfterPromptReceivedPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    public SoraAfterPromptReceivedPanel(ObjectProvider<RegistryService> registryServiceProvider, TgBot tgBot) {
        super(registryServiceProvider, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.SORA_2_AFTER_PROMPT_RECEIVED;
    }

    private String getText() {
        return """
                ⏳ Отлично! Я получил твоё описание. Генерация видео займёт ~3 минуты. Как только ролик будет готов, я пришлю его сюда! 🎬
                """;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(SORA_2_GENERATE_NEW).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
