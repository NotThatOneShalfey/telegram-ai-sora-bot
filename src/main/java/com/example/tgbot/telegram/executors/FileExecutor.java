package com.example.tgbot.telegram.executors;

import com.example.tgbot.telegram.TgBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class FileExecutor {
    private final TgBot tgBot;
    public FileExecutor(TgBot tgBot) {
        this.tgBot = tgBot;
    }

    public File executeFile(GetFile file) {
        try {
            return tgBot.execute(file);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
