package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.configurations.ModelRequestOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.ChatState;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.buttons.ButtonType.*;

@Component
public class KlingSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public KlingSetupPanel(ObjectProvider<RegistryService> registryService, TgBot tgBot) {
        super(registryService, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        session.getChatContext().setState(ChatState.WAITING_FOR_TEXT);
        super.executeSendMessage(session, getText(session), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.KLING_SETUP;
    }

    private String getText(UserSession session) {
        ModelRequestOptions options = session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0);
        return """
                ✍ Отправить текстовое описание сцены или
                🖼 Отправить изображение + описание анимации
                Если отправите только текст — видео будет создано с нуля.
                Если добавите изображение — оно станет основой сцены.
                💡 Чем подробнее описание — тем лучше результат.
                ______________________________________
                ПАРАМЕТРЫ
                %s
                _____________________________________
                💸 СТОИМОСТЬ: {price} монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                """.formatted(options.getOptionsText()).replaceAll("\\{price}", String.valueOf(options.getPrice()));
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(KLING_BACK_TO_MODEL_SELECTION).getKeyboardButton(),
                super.getButton(KLING_FORMAT_SELECTION).getKeyboardButton()));
        rows.add(List.of(super.getButton(KLING_DURATION_SELECTION).getKeyboardButton(),
                super.getButton(KLING_SOUND_SELECTION).getKeyboardButton()));
        rows.add(List.of(super.getButton(KLING_SET_PRO_MOD).getKeyboardButton(),
                super.getButton(KLING_MULTISET_SELECTION).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
