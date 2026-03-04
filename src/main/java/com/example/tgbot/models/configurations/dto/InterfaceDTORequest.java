package com.example.tgbot.models.configurations.dto;

import com.example.tgbot.models.enums.GenerationModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class InterfaceDTORequest {
    private GenerationModel model;
    /** User.telegramId */
    private Long userId;
    private String optionsBody;
}
