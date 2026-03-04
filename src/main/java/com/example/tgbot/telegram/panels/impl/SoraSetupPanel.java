package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.models.configurations.SoraOptions;
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
public class SoraSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public SoraSetupPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
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
                ______________________________________
                %s
                _____________________________________
                💸 СТОИМОСТЬ: {price} монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                """.formatted(options.getOptionsText()).replaceAll("\\{price}", String.valueOf(options.getPrice()));
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
