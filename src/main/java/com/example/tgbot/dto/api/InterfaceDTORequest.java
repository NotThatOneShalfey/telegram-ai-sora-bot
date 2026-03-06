package com.example.tgbot.dto.api;

import com.example.tgbot.domain.enums.GenerationModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InterfaceDTORequest {
    private GenerationModel model;
    /** User.telegramId */
    private Long userId;
    private String optionsBody;
}
