package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.buttons.SunoMusicGenreButton;
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
public class GenerateMusicPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public String getLabel() {
        return "generate_music";
    }

    public static String callback() {
        return "generate_music";
    }

    private String getText() {
        String text = """
                🎵Suno v5 — создаёт полноценную песню за 1–2 минуты: нейросеть сама напишет текст, подберёт вокал и создаст музыку в выбранном стиле.
                Выберите стиль будущей песни 👇
                """;
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        SunoMusicGenreButton prevGenre = null;
        for (SunoMusicGenreButton genre : SunoMusicGenreButton.values()) {
            if (prevGenre == null) {
                prevGenre = genre;
            } else {
                rows.add(List.of(createButton(prevGenre.getButtonText(), CallbackHandler.wrapCallback(SunoSetupPanel.callback(), prevGenre.getButtonCallback()))
                        , createButton(genre.getButtonText(), CallbackHandler.wrapCallback(SunoSetupPanel.callback(), genre.getButtonCallback()))));
                prevGenre = null;
            }
        }
        rows.add(List.of(createButton("Главное меню", MainMenuPanel.callback())));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
