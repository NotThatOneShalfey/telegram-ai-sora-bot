package com.example.tgbot.web.callbacks.keiai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeiAiCallbackDataBlock {
    @JsonProperty("audio_url")
    String audioUrl;
    @JsonProperty("image_url")
    String imageUrl;
    String prompt;
}
