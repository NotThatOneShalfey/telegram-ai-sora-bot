package com.example.tgbot.telegram.panel.impl;

import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.domain.value.Operation;
import com.example.tgbot.registry.ButtonRegistry;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.button.IButton;
import com.example.tgbot.telegram.panel.PanelHelper;
import com.example.tgbot.util.ErrorMessageHelper;
import com.example.tgbot.telegram.session.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public abstract class AbstractSimpleMessagePanel {
    protected final ButtonRegistry buttonRegistry;
    private final TgBot tgBot;

    protected AbstractSimpleMessagePanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        this.buttonRegistry = buttonRegistry;
        this.tgBot = tgBot;
    }

    private void processSendMessageError(String chatId, Exception e) {
        log.error(e.getMessage());
        sendErrorToChat(chatId, Operation.VIDEO_GENERATION, ErrorCode.E010);
    }

    private void processSendFileError(String chatId, Exception e) {
        log.error(e.getMessage());
        sendErrorToChat(chatId, Operation.VIDEO_GENERATION, ErrorCode.E010);
    }

    private void sendErrorToChat(String chatId, Operation operation, ErrorCode errorCode) {
        try {
            tgBot.execute(new SendMessage(chatId, ErrorMessageHelper.forTelegram(operation, errorCode)));
        } catch (TelegramApiException ex) {
            log.error("Failed to send error message to chat {}", chatId, ex);
        }
    }

    protected void executeSendMessage(UserSession session, String text, InlineKeyboardMarkup keyboard, boolean withBalance) {
        SendMessage sm = new SendMessage();
        sm.setChatId(session.getChatId());
        sm.setText(text);
        // Добавляем кнопки
        if (keyboard != null) {
            sm.setReplyMarkup(keyboard);
        }
        // Добавляем баланс
        if (withBalance) {
            PanelHelper.addQuotedBalanceToMessage(sm, session.getUser().getBalance());
        }
        try {
            tgBot.execute(sm);
        } catch (TelegramApiException e) {
            processSendMessageError(session.getChatId(), e);
        }
    }

    protected void executeSendImage(UserSession session, String bytes) {
        try {
            // Шаг 1: Скачать изображение
            URL url = new URL(bytes);
            Path tempFilePath = Files.createTempFile("photo", ".jpg");
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Шаг 2: Отправить в Telegram
            InputFile inputFile = new InputFile(tempFilePath.toFile());
            SendPhoto sendPhoto = new SendPhoto(String.valueOf(session.getChatId()), inputFile);
            tgBot.execute(sendPhoto);

            // Шаг 3: Удалить файл после отправки
            Files.deleteIfExists(tempFilePath);
        } catch (TelegramApiException | IOException | RuntimeException e) {
            log.error("Error sending photo", e);
            processSendFileError(session.getChatId(), e);
        }
    }

    protected void executeSendVideo(UserSession session, String bytes) {
        try {
            SendVideo vid = new SendVideo(String.valueOf(session.getChatId()), new InputFile(bytes));
            vid.setSupportsStreaming(true);
            tgBot.execute(vid);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    protected void executeSendMusic(UserSession session, List<String> bytes) {
        try {
            for (String url : bytes) {
                SendAudio audio = new SendAudio(String.valueOf(session.getChatId()), new InputFile(url));
                tgBot.execute(audio);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    protected IButton getButton(ButtonType buttonType) {
        return buttonRegistry.getButton(buttonType);
    }

}
