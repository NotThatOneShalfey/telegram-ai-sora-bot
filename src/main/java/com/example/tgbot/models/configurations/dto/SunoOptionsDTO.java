package com.example.tgbot.models.configurations.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public class SunoOptionsDTO {
    private boolean customMode;
    private String prompt;
    private boolean instrumental;
    private Integer audioWeight;
    private String genre;
}
