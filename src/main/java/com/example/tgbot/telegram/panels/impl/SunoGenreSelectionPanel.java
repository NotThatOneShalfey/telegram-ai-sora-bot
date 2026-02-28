package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.enums.SunoMusicGenreEnum;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class SunoGenreSelectionPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public SunoGenreSelectionPanel(ObjectProvider<RegistryService> registryServiceProvider, TgBot tgBot) {
        super(registryServiceProvider, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.SUNO_GENRE_SELECTION;
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
        SunoMusicGenreEnum prevGenre = null;
        for (SunoMusicGenreEnum genre : SunoMusicGenreEnum.values()) {
            if (prevGenre == null) {
                prevGenre = genre;
            } else {
                rows.add(List.of(super.getButton(ButtonType.SUNO_GENRE_SELECTION).setParameters(prevGenre).getKeyboardButton(),
                super.getButton(ButtonType.SUNO_GENRE_SELECTION).setParameters(genre).getKeyboardButton()));
                prevGenre = null;
            }
        }
        rows.add(List.of(super.getButton(ButtonType.MAIN_MENU_CALL).getKeyboardButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
