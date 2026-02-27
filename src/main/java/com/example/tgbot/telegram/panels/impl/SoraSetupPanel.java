package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
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
public class SoraSetupPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public SoraSetupPanel(ObjectProvider<RegistryService> registryServiceProvider, TgBot tgBot) {
        super(registryServiceProvider, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        session.getChatContext().setState(ChatState.WAITING_FOR_TEXT);
        super.executeSendMessage(session, getText(session), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.SORA_2_SETUP;
    }

    private String getText(UserSession session) {
        String parameters = session.getModelsConfiguration().get(GenerationModel.SORA_2).getOptionsText();
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
                💸 СТОИМОСТЬ: N монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                """.formatted(parameters);
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
