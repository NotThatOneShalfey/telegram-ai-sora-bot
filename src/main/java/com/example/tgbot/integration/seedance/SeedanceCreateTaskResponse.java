package com.example.tgbot.integration.seedance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Ответ POST /jobs/createTask Seedance API.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeedanceCreateTaskResponse {

    @JsonProperty("taskId")
    private String taskId;
}
