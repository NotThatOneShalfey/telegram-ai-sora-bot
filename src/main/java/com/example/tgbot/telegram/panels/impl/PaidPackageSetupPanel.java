package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.telegram.buttons.PaidPackageButton;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PaidPackageSetupPanel implements IChatPanel {

    private final TgBot bot;

    public PaidPackageSetupPanel(TgBot bot) {
        this.bot = bot;
    }


    @Override
    public void execute(UserSession session) {
        PaidPackageButton pack = session.getPaymentInfo();
        String title = "Покупка пакета";
        String description = "Покупка монет в CreatorLabAi";
        String payload = pack.getButtonName(); // Можно использовать для идентификации заказа

        List<LabeledPrice> prices = new ArrayList<>();
        prices.add(new LabeledPrice("Пакет " + pack.getButtonName(), pack.getPackagePrice()*100)); // цена в копейках (например, 500 = 5.00 у валюты в копейках)
        SendInvoice invoice = SendInvoice.builder()
                .chatId(session.getChatId())
                .title(title)
                .description(description)
                .payload(payload)
                .providerToken(bot.getProviderToken())
                .startParameter("LOAD_PARAMETER{}") // параметр для запусков
                .prices(prices)
                .currency("RUB") // валюта
                .build();
        try {
            bot.execute(invoice);
        } catch (TelegramApiException e) {
            processSendInvoiceError(session.getChatId(), e);
        }
    }

    @Override
    public String getLabel() {
        return "paid_pack_selected";
    }

    public static String callback() {
        return "paid_pack_selected";
    }

    private void processSendInvoiceError(String chatId, TelegramApiException e) {
        log.error(e.getMessage());
        String errorMessage = """
                       \uD83D\uDEA7 Во время оплаты произошла ошибка \uD83D\uDEA7
                            Пожалуйста обратитесь в поддержку @CreativeLabAI
                """;
        try {
            bot.execute(new SendMessage(chatId, errorMessage));
        } catch (TelegramApiException ex) {
            log.error("Во время обработки ошибка возникла ошибка!!!!! {}", e.getMessage());
        }
    }
}
