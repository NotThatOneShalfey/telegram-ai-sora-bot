package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.models.configurations.NanoBananaOptions;
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
public class NanoBananaSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public NanoBananaSetupPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        session.getChatContext().setModel(GenerationModel.NANO_BANANA_PRO);
        session.createNewModelRequestConfiguration(GenerationModel.NANO_BANANA_PRO, NanoBananaOptions.builder().build());
        session.getChatContext().setState(ChatState.WAITING_FOR_TEXT);
        super.executeSendMessage(session, getText(session), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.NANO_BANANA_SETUP;
    }
    private String getText(UserSession session) {
        IModelRequestOptions options = session.getCurrentRequestOptionsByModel(GenerationModel.NANO_BANANA_PRO);
        String text = """
                🖼 Nano Banana Pro — генерация изображений
                
                Отправь:
                ✍️ Текстовый промпт — и я создам изображение с нуля
                или
                🖼 Промпт + картинку — чтобы изменить или доработать загруженное изображение (до 8 изображений)
                %s
                💸 СТОИМОСТЬ: {price} монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                """.formatted(options.getOptionsText()).replaceAll("\\{price}", String.valueOf(options.getPrice()));
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(NANO_BANANA_SELECT_SIZE).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
