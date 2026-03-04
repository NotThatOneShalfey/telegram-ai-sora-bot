package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.registry.ButtonRegistry;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelHelper;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.buttons.ButtonType.*;
import static com.example.tgbot.telegram.buttons.ButtonType.RECHARGE_BALANCE_CALL;

@Component
public class KlingAfterPromptReceivedPanel extends AbstractSimpleMessagePanel implements IChatPanel {
    private final UserService userService;
    public KlingAfterPromptReceivedPanel(ButtonRegistry buttonRegistry, TgBot tgBot, UserService userService) {
        super(buttonRegistry, tgBot);
        this.userService = userService;
    }

    @Override
    public void execute(UserSession session) {
        IModelRequestOptions requestOptions = session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0);
        userService.putOnHold(session, requestOptions.getPrice(), requestOptions.getRequestInput());
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.KLING_AFTER_PROMPT_RECEIVED;
    }

    private String getText() {
        return """
                ⏳ Отлично! Я получил твоё описание. Генерация видео займёт ~3 минуты. Как только ролик будет готов, я пришлю его сюда! 🎬
                """;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(KLING_GENERATE_NEW).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
