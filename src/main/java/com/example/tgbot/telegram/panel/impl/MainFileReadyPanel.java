package com.example.tgbot.telegram.panel.impl;

import com.example.tgbot.integration.kieai.ReceivedFile;
import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panel.IChatPanel;
import com.example.tgbot.telegram.panel.PanelHelper;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.button.ButtonType.*;

@Component
public class MainFileReadyPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public MainFileReadyPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        // Получаем модель, по которой мы отдаем файл
        ReceivedFile receivedFile = session.getReceivedFile();
        GenerationModel model = receivedFile.getModel();

        // Формируем текст
        String prompt = receivedFile.getRequestOptions().getPrompt();
        String dynamic = "Файл";

        if (model.equals(GenerationModel.KLING_3_0) || model.equals(GenerationModel.SORA_2)) {
            dynamic = "Видео готово!";
            executeSendVideo(session, receivedFile.getFirstUrl());
        } else if (model.equals(GenerationModel.SUNO_V5)) {
            dynamic = "Музыкальный трек готов!";
            executeSendMusic(session, receivedFile.getFileUrls());
        } else if (model.equals(GenerationModel.NANO_BANANA_PRO)) {
            dynamic = "Изображение готово!";
            executeSendImage(session, receivedFile.getFirstUrl());
        }
        super.executeSendMessage(session, getText(prompt, dynamic), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.MAIN_SEND_READY_FILE;
    }

    private String getText(String prompt, String dynamicText) {
        String sourceText = """
                ✅ {fileType}
                
                💾 Промпт:
                <blockquote>{prompt}</blockquote>
                """.replaceAll("\\{prompt}", prompt)
                .replaceAll("\\{fileType}", dynamicText);
        return sourceText;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(MAIN_CREATE_IMAGE_CALL).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_CREATE_VIDEO_CALL).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_CREATE_MUSIC_CALL).getKeyboardButton()));
        rows.add(List.of(PanelHelper.getSupportButton(), super.getButton(RECHARGE_BALANCE_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
