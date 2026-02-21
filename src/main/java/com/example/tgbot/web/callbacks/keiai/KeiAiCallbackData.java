package com.example.tgbot.web.callbacks.keiai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeiAiCallbackData {
    String callbackType;
    List<KeiAiCallbackDataBlock> data;
    @JsonProperty("task_id")
    String taskId;
}
