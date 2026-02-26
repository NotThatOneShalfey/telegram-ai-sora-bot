package com.example.tgbot.telegram;

import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

public interface TelegramExecutor {
    void executeMessage(SendMessage sm) throws TelegramApiException;
    void executeInvoice(SendInvoice si) throws TelegramApiException;
    void executePhoto(SendPhoto sp) throws TelegramApiException;
    void executeVideo(SendVideo sp) throws TelegramApiException;
    Object executeAnswerPreCheckout(AnswerPreCheckoutQuery apcq) throws TelegramApiException;
    Map<String, IChatPanel> getPanels();
    Map<String, UserSession> getSessions();
    String getProviderToken();
}
