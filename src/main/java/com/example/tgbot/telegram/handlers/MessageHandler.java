package com.example.tgbot.telegram.handlers;

import com.example.tgbot.RegistryService;
import com.example.tgbot.db.User;
import com.example.tgbot.models.adapters.IRequestAdapter;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.RateLimiterService;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import com.example.tgbot.telegram.executors.FileExecutor;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.panels.impl.MainMenuPanel;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.tgbot.telegram.panels.PanelType.MAIN_MENU;

@Component
@Slf4j
public class MessageHandler {
    private final ObjectProvider<FileExecutor> fileExecutorProvider;
    private final ObjectProvider<RegistryService> registryServiceProvider;
    private final UserService userService;
    private final Map<GenerationModel, IRequestAdapter> adapters = new ConcurrentHashMap<>();
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper mapper = new JsonMapper();

    @Value("${telegram.bot.token}")
    private String botToken;

    public MessageHandler(UserService userService,
                          Collection<IRequestAdapter> adaptersCollection,
                          ObjectProvider<FileExecutor> fileExecutorProvider,
                          ObjectProvider<RegistryService> registryServiceProvider,
                          RateLimiterService rateLimiterService) {
        this.userService = userService;
        this.fileExecutorProvider = fileExecutorProvider;
        this.registryServiceProvider = registryServiceProvider;
        this.rateLimiterService = rateLimiterService;
        adaptersCollection.forEach(a -> adapters.put(a.getModel(), a));
    }


    public void handleMessage(Message message, UserSession session) {
        // Получили айди чата
        Long chatId = message.getChatId();
        User user = userService.findOrCreateUser(chatId);
        // Обработка стартового сообщения
        if (message.hasText() && message.getText().contains("/start")) {
            String referral = null;
            if (message.getText().contains(" ")) {
                referral = message.getText().split(" ")[1];
            }
            handleStart(session, user, message.getFrom().getUserName(), referral);
            return;
        }
        // Если завершение оплаты
        if (message.getSuccessfulPayment() != null) {
            userService.addBalance(user, PaidPackageEnum.getPackagePriceByName(message.getSuccessfulPayment().getInvoicePayload()));
            IChatPanel panel = registryServiceProvider.getObject().getChatPanel(MAIN_MENU);
            if (panel instanceof MainMenuPanel mmp) {
                mmp.setPanelText("Оплата успешно зафиксирована!");
            }
            panel.execute(session);
            return;
        }
        // В конце концов просто обрабатываем так, будто это промпт
        handlePrompt(message, session);
    }

    private void handleStart(UserSession session, User user, String userName, String referralLink) {
        userService.updateUserCredentials(user, userName, referralLink);
        registryServiceProvider.getObject().getChatPanel(MAIN_MENU).execute(session);
    }

    private void handlePrompt(Message message, UserSession session) {
        boolean hasImage = (message.hasDocument() && message.getDocument().getMimeType().contains("image")) || message.hasPhoto();
        String text = message.getCaption() != null ? message.getCaption() : message.hasText() ? message.getText() : null;
        log.trace("Call -> handlePrompt -> hasImage={}, text={}, session={}", hasImage, text, session);
        if (text == null) {
            session.setContextualMessage("Простите, не понял вашего сообщения.");
            registryServiceProvider.getObject().getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        // Первичные проверки
        if (text.length() > 4999) {
            session.setContextualMessage("\uD83D\uDCDD Ваш запрос слишком длинный.\n" +
                    "Попробуйте сократить текст до 5000 символов.");
            registryServiceProvider.getObject().getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        // Apply per-user rate limiting
        if (!rateLimiterService.tryConsume(message.getChatId())) {
            session.setContextualMessage("Превышен лимит запросов. Пожалуйста, подождите и попробуйте позже.");
            registryServiceProvider.getObject().getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        // Заполняем настройки
        if (session.getChatContext().getModel() != null) {
            GenerationModel model = session.getChatContext().getModel();
            // Финальная проверка на то, хватает ли денег на генерацию
            int price = session.getCurrentRequestOptionsByModel(model).getPrice();
            if (!userService.checkBalanceBeforeGeneration(session, price)) {
                session.setContextualMessage("⚠ У вас закончились монеты для создания видео.\n" +
                        "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E");
                registryServiceProvider.getObject().getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
                return;
            }

            try {
                if (hasImage) {
                    handlePromptAndImage(message, session, model);
                } else {
                    handlePlainPrompt(text, session, model);
                }
            } catch (JsonProcessingException e) {
                log.error("Couldn't process prompt. Error -> {}", e.getMessage());
            }
        // Отдаем на исполнение
        adapters.get(model).makeRequest(session);
        }
    }

    private void handlePlainPrompt(String text, UserSession session, GenerationModel model) throws JsonProcessingException {
        log.trace("Call -> handlePlainPrompt");
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", text);
        session.getCurrentRequestOptionsByModel(model).setParametersFromJson(mapper.writeValueAsString(input));
    }

    private void handlePromptAndImage(Message message, UserSession session, GenerationModel model) throws JsonProcessingException {
        log.trace("Call -> handlePromptAndImage");
        String prompt = message.getCaption();
        if (prompt == null) {
            session.setContextualMessage("Не удалось получить текст вместе с изображением. Пожалуйста, отправьте изображение вместе с текстом.");
            registryServiceProvider.getObject().getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        List<String> fileIds = new ArrayList<>();
        if (message.hasPhoto()) {
            fileIds = getBestPhotos(message.getPhoto())
                    .stream()
                    .map(PhotoSize::getFileId)
                    .toList();
        } else if (message.hasDocument()) {
            fileIds.add(message.getDocument().getFileId());
        }

        if (fileIds.isEmpty()) {
            session.setContextualMessage("Не удалось получить файл изображения.");
            registryServiceProvider.getObject().getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }

        List<String> imageUrls = new ArrayList<>();
        for (String s : fileIds) {
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(s);
            File file = fileExecutorProvider.getObject().executeFile(getFileRequest);
            imageUrls.add("https://api.telegram.org/file/bot" + botToken + "/" + file.getFilePath());
        }


        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        if (model.equals(GenerationModel.NANO_BANANA_PRO)) {
            input.put("imageInput", imageUrls);
        } else {
            input.put("imageUrls", imageUrls);
        }
        if (model.equals(GenerationModel.SORA_2)) {
            input.put("model", GenerationModel.SORA_2_WITH_IMAGE);
        }
        session.getCurrentRequestOptionsByModel(model).setParametersFromJson(mapper.writeValueAsString(input));
    }

//    public List<PhotoSize> getBestPhotos(List<PhotoSize> photos, int photoCount) {
//        if (photos.size() < photoCount) return photos;
//        return photos.subList(photos.size() - photoCount, photos.size());
//    }

    public List<PhotoSize> getBestPhotos(List<PhotoSize> photos) {
        Map<String, PhotoSize> groups = new LinkedHashMap<>();

        for (PhotoSize photo : photos) {
            String key = photo.getFileUniqueId().substring(0, Math.min(15, photo.getFileUniqueId().length()));

            groups.merge(key, photo, (existing, newPhoto) ->
                    newPhoto.getFileSize() > existing.getFileSize() ? newPhoto : existing
            );
        }

        return new ArrayList<>(groups.values());
    }

}
