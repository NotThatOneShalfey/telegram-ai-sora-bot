package com.example.tgbot.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunoOptionsDTO {
    private boolean customMode;
    private String prompt;
    private boolean instrumental;
    private Integer audioWeight;
    private String genre;
}
