package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.configurations.ModelRequestOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.ChatState;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class SunoAfterGenreSelectionPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public SunoAfterGenreSelectionPanel(RegistryService registryService, TgBot tgBot) {
        super(registryService, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        session.getChatContext().setState(ChatState.WAITING_FOR_TEXT);
        session.getChatContext().setModel(GenerationModel.SUNO_V5);
        ModelRequestOptions sunoOptions = session.getModelsConfiguration().get(GenerationModel.SUNO_V5);
        super.executeSendMessage(session, getText(sunoOptions.getOptionsText()), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.SUNO_SETUP;
    }

    private String getText(String genre) {
        String text = """
                Отлично, с жанром определились!
                Напиши пару предложений о том, про кого или про что будет песня. Чем больше подробностей, тем круче получится!
                                
                Жанр: {genre}
                                
                💸 СТОИМОСТЬ: 299 монет 💸
                                
                🪙1 монета = 1 рубль 🪙
                Отправь мне сообщение и я сгенерирую песню 👇
                """.replaceAll("\\{genre}", genre);
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(registryService.getButton(ButtonType.MAIN_MENU_CALL).getKeyboardButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
