package com.example.tgbot.telegram.panel.impl;

import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.integration.config.SunoOptions;
import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.panel.IChatPanel;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.ChatState;
import com.example.tgbot.telegram.session.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class SunoSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    private final com.example.tgbot.service.PriceRegistryService priceRegistryService;

    public SunoSetupPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot,
                          com.example.tgbot.service.PriceRegistryService priceRegistryService) {
        super(buttonRegistry, tgBot);
        this.priceRegistryService = priceRegistryService;
    }

    @Override
    public void execute(UserSession session) {
        session.getChatContext().setModel(GenerationModel.SUNO_V5);
        session.createNewModelRequestConfiguration(GenerationModel.SUNO_V5, SunoOptions.builder().build());
        session.getChatContext().setState(ChatState.WAITING_FOR_TEXT);
        super.executeSendMessage(session, getText(session), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.SUNO_SETUP;
    }

    private String getText(UserSession session) {
        IModelRequestOptions options = session.getCurrentRequestOptionsByModel(GenerationModel.SUNO_V5);
        String text = """
                Отлично, с жанром определились!
                Напиши пару предложений о том, про кого или про что будет песня. Чем больше подробностей, тем круче получится!
                {parameters}
                💸 СТОИМОСТЬ: {price} монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                Отправь мне сообщение и я сгенерирую песню 👇
                """.replaceAll("\\{parameters}", options.getOptionsText()).replaceAll("\\{price}", String.valueOf(priceRegistryService.calculatePrice(GenerationModel.SUNO_V5, options, session.getUser())));
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(super.getButton(ButtonType.MAIN_MENU_CALL).getKeyboardButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
