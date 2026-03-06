package com.example.tgbot.telegram.panel.impl;

import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.integration.config.SoraOptions;
import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panel.IChatPanel;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.ChatState;
import com.example.tgbot.telegram.session.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.button.ButtonType.*;

@Component
public class SoraSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    private final com.example.tgbot.service.PriceRegistryService priceRegistryService;

    public SoraSetupPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot,
                          com.example.tgbot.service.PriceRegistryService priceRegistryService) {
        super(buttonRegistry, tgBot);
        this.priceRegistryService = priceRegistryService;
    }

    @Override
    public void execute(UserSession session) {
        session.getChatContext().setModel(GenerationModel.SORA_2);
        session.createNewModelRequestConfiguration(GenerationModel.SORA_2, SoraOptions.builder().build());
        session.getChatContext().setState(ChatState.WAITING_FOR_TEXT);
        super.executeSendMessage(session, getText(session), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.SORA_2_SETUP;
    }

    private String getText(UserSession session) {
        IModelRequestOptions options = session.getCurrentRequestOptionsByModel(GenerationModel.SORA_2);
        return """
                ✍ Отправить текстовое описание сцены или
                🖼 Отправить изображение + описание анимации
                Если отправите только текст — видео будет создано с нуля.
                Если добавите изображение — оно станет основой сцены.
                💡 Чем подробнее описание — тем лучше результат.
                %s
                💸 СТОИМОСТЬ: {price} монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                """.formatted(options.getOptionsText()).replaceAll("\\{price}", String.valueOf(priceRegistryService.calculatePrice(GenerationModel.SORA_2, options, session.getUser())));
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(SORA_2_BACK_TO_MODEL_SELECTION).getKeyboardButton(),
                super.getButton(SORA_2_SELECT_FORMAT).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }


}
