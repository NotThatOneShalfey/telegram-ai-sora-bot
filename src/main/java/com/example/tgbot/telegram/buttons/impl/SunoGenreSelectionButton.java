package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import com.example.tgbot.telegram.buttons.enums.SunoMusicGenreEnum;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SunoGenreSelectionButton implements IButton {
    @Lazy
    private final PanelRegistry panelRegistry;
    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public ButtonType getLabel() {
        return ButtonType.SUNO_GENRE_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton(Object... parameters) {
        SunoMusicGenreEnum genre = parseGenre(parameters);
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(genre.getButtonText());
        button.setCallbackData(getLabel().toString() + "::" + genre);
        return button;
    }

    private static SunoMusicGenreEnum parseGenre(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof SunoMusicGenreEnum smge) return smge;
            try {
                if (o != null) return SunoMusicGenreEnum.valueOf(o.toString());
            } catch (IllegalArgumentException ignored) {}
        }
        return SunoMusicGenreEnum.ROCK; // default
    }

    @Override
    public void executeOnCallback(UserSession session, String[] parameters) {
        SunoMusicGenreEnum genre = parameters.length >= 1 ? SunoMusicGenreEnum.valueOf(parameters[0]) : SunoMusicGenreEnum.ROCK;
        session.getCurrentRequestOptionsByModel(GenerationModel.SUNO_V5).setParametersFromJson(getJsonForOptionsChange(genre));
        panelRegistry.getChatPanel(PanelType.SUNO_SETUP).execute(session);
    }

    private String getJsonForOptionsChange(SunoMusicGenreEnum genre) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("genre", genre.getValue());
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
