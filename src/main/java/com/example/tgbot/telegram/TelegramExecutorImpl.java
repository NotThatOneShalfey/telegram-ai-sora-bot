package com.example.tgbot.telegram;

import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TelegramExecutorImpl implements TelegramExecutor {

    @Override
    public void executeMessage(SendMessage sm) throws TelegramApiException {
        TgBot.getInstance().execute(sm);
    }

    @Override
    public void executeInvoice(SendInvoice si) throws TelegramApiException {
        TgBot.getInstance().execute(si);
    }

    @Override
    public void executePhoto(SendPhoto sp) throws TelegramApiException {
        TgBot.getInstance().execute(sp);
    }

    @Override
    public void executeVideo(SendVideo sp) throws TelegramApiException {
        TgBot.getInstance().execute(sp);
    }

    @Override
    public Object executeAnswerPreCheckout(AnswerPreCheckoutQuery apcq) throws TelegramApiException {
        return TgBot.getInstance().execute(apcq);
    }

    @Override
    public Map<String, IChatPanel> getPanels() {
        return TgBot.getInstance().getPanels();
    }

    @Override
    public Map<String, UserSession> getSessions() {
        return TgBot.getInstance().getSessions();
    }

    @Override
    public String getProviderToken() {
        return TgBot.getInstance().getProviderToken();
    }


}
