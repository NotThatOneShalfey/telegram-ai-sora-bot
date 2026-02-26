package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.models.configurations.ModelRequestOptions;
import com.example.tgbot.models.configurations.SunoOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.ChatState;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.panels.PanelHelper.createButton;

@Component
public class SunoSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    @Override
    public void execute(UserSession session) {
        ModelRequestOptions sunoOptions = session.getModelsConfiguration().computeIfAbsent(GenerationModel.SUNO_V5, model -> SunoOptions.builder()
                .audioWeight(null)
                .customMode(false)
                .instrumental(false)
                .build());
        session.getChatContext().setState(ChatState.WAITING_FOR_TEXT);
        session.getChatContext().setModel(GenerationModel.SUNO_V5);
        super.executeSendMessage(session, getText(sunoOptions.getOptionsText()), getKeyboard(), true);
    }

    @Override
    public String getLabel() {
        return "suno_music_selected";
    }

    public static String callback() {
        return "suno_music_selected";
    }

    private String getText(String genre) {
        String text = """
                Отлично, с жанром определились!
                Напиши пару предложений о том, про кого или про что будет песня. Чем больше подробностей, тем круче получится!
                                
                Жанр: {genre}
                                
                💸 СТОИМОСТЬ: 299 монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                Отправь мне сообщение и я сгенерирую песню 👇
                """.replaceAll("\\{genre}", genre);
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("Главное меню", MainMenuPanel.callback())));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
