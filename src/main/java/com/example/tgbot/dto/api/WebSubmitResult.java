package com.example.tgbot.dto.api;

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
