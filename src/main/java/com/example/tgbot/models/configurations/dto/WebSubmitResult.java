package com.example.tgbot.models.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSubmitResult {
    private String taskId;
    private int balance;
}
