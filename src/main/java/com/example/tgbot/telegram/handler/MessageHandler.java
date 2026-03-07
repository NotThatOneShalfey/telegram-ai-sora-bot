package com.example.tgbot.telegram.handler;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.model.User;
import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.domain.value.Operation;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.registry.AdapterRegistry;
import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.service.PriceRegistryService;
import com.example.tgbot.service.RateLimiterService;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.button.enums.PaidPackageEnum;
import com.example.tgbot.telegram.collector.TelegramMediaBatchCollector;
import com.example.tgbot.telegram.executor.FileExecutor;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import com.example.tgbot.util.ErrorMessageHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.util.*;

import static com.example.tgbot.telegram.panel.PanelType.MAIN_MENU;

@Component
@Slf4j
public class MessageHandler {
    private final ObjectProvider<FileExecutor> fileExecutorProvider;
    private final PanelRegistry panelRegistry;
    private final AdapterRegistry adapterRegistry;
    private final UserService userService;
    private final RateLimiterService rateLimiterService;
    private final TelegramMediaBatchCollector mediaBatchCollector;
    private final PriceRegistryService priceRegistryService;
    private final ObjectMapper mapper = new JsonMapper();

    public MessageHandler(ObjectProvider<FileExecutor> fileExecutorProvider, @Lazy PanelRegistry panelRegistry,
                          @Lazy AdapterRegistry adapterRegistry, UserService userService, RateLimiterService rateLimiterService,
                          TelegramMediaBatchCollector mediaBatchCollector, PriceRegistryService priceRegistryService) {
        this.fileExecutorProvider = fileExecutorProvider;
        this.panelRegistry = panelRegistry;
        this.adapterRegistry = adapterRegistry;
        this.userService = userService;
        this.rateLimiterService = rateLimiterService;
        this.mediaBatchCollector = mediaBatchCollector;
        this.priceRegistryService = priceRegistryService;
    }

    @Value("${telegram.bot.token}")
    private String botToken;


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
            session.setContextualMessage("Оплата успешно зафиксирована!");
            panelRegistry.getChatPanel(MAIN_MENU).execute(session);
            return;
        }
        // В конце концов просто обрабатываем так, будто это промпт
        handlePrompt(message, session);
    }

    private void handleStart(UserSession session, User user, String userName, String referralLink) {
        userService.updateUserCredentials(user, userName, referralLink);
        panelRegistry.getChatPanel(MAIN_MENU).execute(session);
    }

    private void handlePrompt(Message message, UserSession session) {
        boolean hasImage = (message.hasDocument() && message.getDocument().getMimeType() != null && message.getDocument().getMimeType().contains("image")) || message.hasPhoto();
        // Если сообщение с media_group_id (альбом) — буферизуем и обработаем батчем
        if (hasImage && mediaBatchCollector.offer(message, session)) {
            return;
        }
        String text = message.getCaption() != null ? message.getCaption() : message.hasText() ? message.getText() : null;
        log.trace("Call -> handlePrompt -> hasImage={}, text={}, session={}", hasImage, text, session);
        if (text == null) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E005));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        // Первичные проверки
        if (text.length() > 4999) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E005));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        // Apply per-user rate limiting
        if (!rateLimiterService.tryConsume(message.getChatId())) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E009));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        // Заполняем настройки
        if (session.getChatContext().getModel() != null) {
            GenerationModel model = session.getChatContext().getModel();
            // Финальная проверка на то, хватает ли денег на генерацию
            IModelRequestOptions options = session.getCurrentRequestOptionsByModel(model);
            int price = priceRegistryService.calculatePrice(model, options, session.getUser());
            if (!userService.checkBalanceBeforeGeneration(session, price)) {
                session.setContextualMessage(ErrorMessageHelper.forTelegram(Operation.fromModel(model), ErrorCode.E004));
                panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
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
                session.setContextualMessage(ErrorMessageHelper.forTelegram(Operation.fromModel(model), ErrorCode.E005));
                panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
                return;
            }
            adapterRegistry.getAdapter(model).makeRequest(session);
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
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E005));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        List<String> fileIds = new ArrayList<>();
        if (message.hasPhoto()) {
            fileIds = getBestPhotos(message.getPhoto()).stream().map(PhotoSize::getFileId).toList();
        } else if (message.hasDocument()) {
            fileIds.add(message.getDocument().getFileId());
        }
        if (fileIds.isEmpty()) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E006));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        handlePromptWithFileIds(prompt, fileIds, session, model);
    }

//    public List<PhotoSize> getBestPhotos(List<PhotoSize> photos, int photoCount) {
//        if (photos.size() < photoCount) return photos;
//        return photos.subList(photos.size() - photoCount, photos.size());
//    }

    /**
     * Обрабатывает батч из альбома: prompt и fileIds уже извлечены коллектором.
     */
    public void handleMediaGroupBatch(String prompt, List<String> fileIds, UserSession session) {
        if (prompt == null || prompt.isBlank()) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E005));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        if (prompt.length() > 4999) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E005));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        if (fileIds == null || fileIds.isEmpty()) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E006));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }

        if (!rateLimiterService.tryConsume(session.getUser().getTelegramId())) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E009));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }

        GenerationModel model = session.getChatContext().getModel();
        if (model == null) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(ErrorCode.E005));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }
        IModelRequestOptions opts = session.getCurrentRequestOptionsByModel(model);
        int price = priceRegistryService.calculatePrice(model, opts, session.getUser());
        if (!userService.checkBalanceBeforeGeneration(session, price)) {
            session.setContextualMessage(ErrorMessageHelper.forTelegram(Operation.fromModel(model), ErrorCode.E004));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
            return;
        }

        try {
            handlePromptWithFileIds(prompt, fileIds, session, model);
            adapterRegistry.getAdapter(model).makeRequest(session);
        } catch (JsonProcessingException e) {
            log.error("Couldn't process media batch. Error -> {}", e.getMessage());
            session.setContextualMessage(ErrorMessageHelper.forTelegram(Operation.fromModel(model), ErrorCode.E005));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
        }
    }

    private void handlePromptWithFileIds(String prompt, List<String> fileIds, UserSession session, GenerationModel model) throws JsonProcessingException {
        List<String> imageUrls = new ArrayList<>();
        for (String fileId : fileIds) {
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(fileId);
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
