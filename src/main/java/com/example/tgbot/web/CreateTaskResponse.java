package com.example.tgbot.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTaskResponse {
    private int code;
    private String message;
    private CreateTaskData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateTaskData {
        @JsonProperty("taskId")
        private String taskId;
    }
}