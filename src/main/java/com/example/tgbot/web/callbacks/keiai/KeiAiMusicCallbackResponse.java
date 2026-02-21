package com.example.tgbot.web.callbacks.keiai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeiAiMusicCallbackResponse {
    int code;
    KeiAiCallbackData data;
}
