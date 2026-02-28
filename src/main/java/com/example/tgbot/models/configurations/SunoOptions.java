package com.example.tgbot.models.configurations;

import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.enums.AspectRatioEnum;
import com.example.tgbot.telegram.buttons.enums.SunoMusicGenreEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Builder
@Setter
@ToString
public class SunoOptions implements ModelRequestOptions {
    private final ObjectMapper mapper = new JsonMapper();
    @Builder.Default
    @Getter
    private final GenerationModel model = GenerationModel.SUNO_V5;
    @Builder.Default
    private boolean customMode = true;
    @Getter
    private String prompt;
    private boolean instrumental;
    @Builder.Default
    private Integer audioWeight = null;
    private String genre;



    @Override
    public int getPrice() {
        return 299;
    }

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "V5");
        payload.put("customMode", customMode);

        String resultingPrompt = "Жанр: " + genre + " Описание: " + prompt;
        payload.put("prompt", resultingPrompt);
        payload.put("instrumental", false);
        payload.put("audioWeight", null);

        return payload;
    }

    @Override
    public String getOptionsText() {
        String text = """
                
                ПАРАМЕТРЫ
                Модель: {0}
                Жанр: {1}
                
                """;

        return MessageFormat.format(text,
                model.getLocalizedModelName(),
                SunoMusicGenreEnum.getButtonTextByValue(genre)
        );
    }

    @Override
    public void setParametersFromJson(String json) {
        try {
            mapper.updateValue(this, mapper.readTree(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
