package com.example.tgbot.telegram.buttons.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.enums.SunoMusicGenreEnum;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@RequiredArgsConstructor
public class SunoGenreSelectionButton implements IButton {
    private final RegistryService registryService;
    private final ObjectMapper mapper = new JsonMapper();

    private SunoMusicGenreEnum genre;

    @Override
    public ButtonType getLabel() {
        return ButtonType.SUNO_GENRE_SELECTION;
    }

    @Override
    public InlineKeyboardButton getKeyboardButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(genre.getButtonText());
        button.setCallbackData(getLabel().toString() + "::" + genre.getButtonCallback());
        return button;
    }

    @Override
    public IButton setParameters(Object... parameters) {
        for (Object o : parameters) {
            if (o instanceof SunoMusicGenreEnum sge) {
                this.genre = sge;
            }
        }
        return this;
    }

    @Override
    public void executeOnCallback(UserSession session) {
        // Заполняем параметр в конфиге для модели
        String str;
        try {
            str = mapper.writeValueAsString("\"genre\":" + genre.getButtonValueForOptions());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        session.getModelsConfiguration().get(GenerationModel.SUNO_V5).setParametersFromJson(str);
        registryService.getChatPanel(PanelType.SUNO_AFTER_GENRE_SELECTION).execute(session);
    }
}
