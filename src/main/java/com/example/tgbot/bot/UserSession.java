package com.example.tgbot.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {
    private BotState state;
    private String selectedFormat; // e.g. "16:9" or "9:16"
}