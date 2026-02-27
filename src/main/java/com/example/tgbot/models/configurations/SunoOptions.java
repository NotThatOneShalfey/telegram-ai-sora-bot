package com.example.tgbot.models.configurations;

import com.example.tgbot.models.enums.GenerationModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Builder
@Setter
public class SunoOptions implements ModelRequestOptions {
    private final ObjectMapper mapper = new JsonMapper();

    private final GenerationModel model = GenerationModel.SUNO_V5;
    private boolean customMode;
    private String prompt;
    private boolean instrumental;
    private Integer audioWeight;
    private String genre;



    @Override
    public int getPrice() {
        return 0;
    }

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "V5");
        payload.put("customMode", false);

        String resultingPrompt = "Жанр: " + genre + " Описание: " + prompt;
        payload.put("prompt", resultingPrompt);
        payload.put("instrumental", false);
        payload.put("audioWeight", null);

        return payload;
    }

    @Override
    public String getOptionsText() {
        return null;
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
