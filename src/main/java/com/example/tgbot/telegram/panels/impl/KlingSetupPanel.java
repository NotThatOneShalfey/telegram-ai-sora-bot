package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.models.configurations.KlingOptions;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.ChatState;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.buttons.ButtonType.*;

@Component
public class KlingSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    private final com.example.tgbot.service.PriceRegistryService priceRegistryService;

    public KlingSetupPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot,
                           com.example.tgbot.service.PriceRegistryService priceRegistryService) {
        super(buttonRegistry, tgBot);
        this.priceRegistryService = priceRegistryService;
    }

    @Override
    public void execute(UserSession session) {
        session.getChatContext().setModel(GenerationModel.KLING_3_0);
        session.createNewModelRequestConfiguration(GenerationModel.KLING_3_0, KlingOptions.builder().build());
        session.getChatContext().setState(ChatState.WAITING_FOR_TEXT);
        super.executeSendMessage(session, getText(session), getKeyboard(session), true);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.KLING_SETUP;
    }

    private String getText(UserSession session) {
        IModelRequestOptions options = session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0);
        return """
                ✍️ Отправить текстовое описание сцены или

                🖼 Отправить изображение +
                описание анимации

                💡 Чем подробнее описание — тем лучше результат.
                %s
                💸 СТОИМОСТЬ: {price} монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                """.formatted(options.getOptionsText()).replaceAll("\\{price}", String.valueOf(priceRegistryService.calculatePrice(GenerationModel.KLING_3_0, options, session.getUser())));
    }

    private InlineKeyboardMarkup getKeyboard(UserSession session) {
        KlingOptions options = (KlingOptions) session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0);
        boolean proMode = "pro".equalsIgnoreCase(options.getMode());
        boolean withSound = options.isWithSound();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(KLING_BACK_TO_MODEL_SELECTION).getKeyboardButton(),
                super.getButton(KLING_FORMAT_SELECTION).getKeyboardButton()));
        rows.add(List.of(super.getButton(KLING_DURATION_SELECTION).getKeyboardButton(),
                super.getButton(KLING_SOUND_SELECTION).getKeyboardButton(withSound)));
        rows.add(List.of(super.getButton(KLING_SET_PRO_MOD).getKeyboardButton(proMode)));
        rows.add(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
