package com.example.tgbot.bot;

import com.example.tgbot.model.User;
import com.example.tgbot.service.UserService;
import com.example.tgbot.service.VideoGenerationService;
import com.example.tgbot.service.RateLimiterService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;


@Component
@Slf4j
public class SoraVideoBot extends TelegramWebhookBot {

    private final UserService userService;
    private final VideoGenerationService videoGenerationService;
    private final RateLimiterService rateLimiterService;
    private final Executor taskExecutor;

    public SoraVideoBot(UserService userService,
                        VideoGenerationService videoGenerationService,
                        RateLimiterService rateLimiterService,
                        @Qualifier("botExecutor") Executor taskExecutor) {
        this.userService = userService;
        this.videoGenerationService = videoGenerationService;
        this.rateLimiterService = rateLimiterService;
        this.taskExecutor = taskExecutor;
    }


    private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.webhook-path:}")
    private String webhookPath;

    @PostConstruct
    void init() {
        log.info("SoraVideoBot initialized with name {}", botName);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return webhookPath;
    }

    @Override
    public org.telegram.telegrambots.meta.api.methods.BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        log.debug("Update Received: {}", update);
        taskExecutor.execute(() -> {
            try {
                processUpdate(update);
            } catch (Exception e) {
                log.error("Unhandled exception while processing update", e);
            }
        });
        return null;
    }

    private void processUpdate(Update update) {
        log.trace("Call processUpdate");
        try {
            if (update.hasCallbackQuery()) {
                log.trace("update has CallbackQuery");
                handleCallback(update.getCallbackQuery());
                return;
            }
            if (update.hasMessage()) {
                log.trace("update has Message");
                Message message = update.getMessage();
                Long chatId = message.getChatId();
                UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession(BotState.INITIAL, null));

                if (message.hasText()) {
                    log.trace("Message has Text");
                    String text = message.getText();
                    if ("/start".equalsIgnoreCase(text)) {
                        handleStart(chatId);
                        return;
                    }
                    switch (session.getState()) {
                        case WAITING_FOR_TEXT_DESCRIPTION:
                            handleTextDescription(chatId, text, session);
                            break;
                        default:
                            // unknown message in current state
                            sendMainMenu(chatId, "Я не понял вашу команду. Пожалуйста, выберите действие из меню.");
                    }
                } else if (message.hasPhoto()) {
                    log.trace("Message has Photo");
                    if (session.getState() == BotState.WAITING_FOR_IMAGE_UPLOAD) {
                        handleImageUpload(chatId, message, session);
                    } else {
                        sendMainMenu(chatId, "Фото получено, но я ожидаю другую команду. Выберите действие из меню.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }


    private void handleStart(Long chatId) throws TelegramApiException {
        // Persist or retrieve the user
        User user = userService.findOrCreateUser(chatId);
        sessions.put(chatId, new UserSession(BotState.WAITING_FOR_PACKAGE_SELECTION, null));
        String text = "Привет! Это бот Sora 2 - генератор видео с помощью ИИ\n\n" +
                "Тут ты можешь посмотреть примеры и как правильно их отправлять : ССЫЛКА\n" +
                "Инструкция как пользоваться ботом: ССЫЛКА\n" +
                "Пользовательское соглашение (ссылка)\n" +
                "Политика конфиденциальности (ссылка)";
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(packageKeyboard());
        execute(message);
    }

    private void handleCallback(CallbackQuery callback) throws TelegramApiException {
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();
        UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession(BotState.INITIAL, null));
        User user = userService.findOrCreateUser(chatId);
        log.debug("Received callback {} from {}", data, chatId);
        switch (data) {
            case "package_1":
                userService.addBalance(user, 1);
                sendAfterPurchase(chatId, 1);
                break;
            case "package_5":
                userService.addBalance(user, 5);
                sendAfterPurchase(chatId, 5);
                break;
            case "package_50":
                userService.addBalance(user, 50);
                sendAfterPurchase(chatId, 50);
                break;
            case "package_gift":

                userService.addBalance(user, 1);
                sendAfterPurchase(chatId, 1);
                break;
            case "main_generate_text":
                if (user.getBalance() <= 0) {
                    sendMainMenu(chatId, "У вас недостаточно генераций. Пожалуйста, пополните баланс.");
                } else {
                    session.setState(BotState.WAITING_FOR_FORMAT_SELECTION);
                    sendFormatSelection(chatId, user.getBalance());
                }
                break;
            case "main_generate_image":
                if (user.getBalance() <= 0) {
                    sendMainMenu(chatId, "У вас недостаточно генераций. Пожалуйста, пополните баланс.");
                } else {
                    session.setState(BotState.WAITING_FOR_IMAGE_UPLOAD);
                    session.setSelectedFormat(null);
                    sendImageUploadPrompt(chatId, user.getBalance());
                }
                break;
            case "main_recharge":

                session.setState(BotState.WAITING_FOR_PACKAGE_SELECTION);
                SendMessage pkgMsg = new SendMessage(String.valueOf(chatId), "Выберите пакет для пополнения баланса:");
                pkgMsg.setReplyMarkup(packageKeyboard());
                execute(pkgMsg);
                break;
            case "format_16_9":
                session.setSelectedFormat("16:9");
                session.setState(BotState.WAITING_FOR_TEXT_DESCRIPTION);
                sendDescriptionPrompt(chatId, user.getBalance());
                break;
            case "format_9_16":
                session.setSelectedFormat("9:16");
                session.setState(BotState.WAITING_FOR_TEXT_DESCRIPTION);
                sendDescriptionPrompt(chatId, user.getBalance());
                break;
            case "format_back":
                // return to main menu
                session.setState(BotState.INITIAL);
                sendMainMenu(chatId, "Возвращаюсь в главное меню.");
                break;
            case "menu_back":
                session.setState(BotState.INITIAL);
                sendMainMenu(chatId, "Возвращаюсь в главное меню.");
                break;
            default:

                break;
        }

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callback.getId());
        execute(answer);
    }

    private void sendAfterPurchase(Long chatId, int purchasedAmount) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        sessions.get(chatId).setState(BotState.INITIAL);
        String text = String.format("Поздравляем, у вас доступно %d видео\n\n" +
                "Тут ты можешь посмотреть примеры и шаблоны : ССЫЛКА\n" +
                "Инструкция как пользоваться ботом: ССЫЛКА", user.getBalance());
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setReplyMarkup(mainMenuKeyboard());
        execute(msg);
    }


    private void sendMainMenu(Long chatId, String text) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        if (text == null) {
            text = "Главное меню";
        }
        SendMessage message = new SendMessage(String.valueOf(chatId), text + "\n\nОсталось генераций: " + user.getBalance());
        message.setReplyMarkup(mainMenuKeyboard());
        execute(message);
    }

    private void sendFormatSelection(Long chatId, int balance) throws TelegramApiException {
        String text = String.format("Модель для генерации: Sora 2\nУ вас доступно %d генераций\nВыберите формат", balance);
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(formatKeyboard());
        execute(message);
    }

    private void sendDescriptionPrompt(Long chatId, int balance) throws TelegramApiException {
        String text = String.format(
                "Модель для генерации Sora 2\nУ вас доступно %d генераций\nВведите описание своего видео.\n\n" +
                "Тут ты можешь посмотреть примеры и шаблоны : ССЫЛКА\n" +
                "Гайд по генерации видео", balance);
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(backToMenuKeyboard());
        execute(message);
    }

    private void sendImageUploadPrompt(Long chatId, int balance) throws TelegramApiException {
        String text = String.format(
                "Модель для генерации: Sora 2\nУ вас доступно %d генераций\n" +
                "Отправьте изображение для генерации видео (JPEG, PNG, WEBP).\n\n" +
                "Тут ты можешь посмотреть примеры и шаблоны : ССЫЛКА", balance);
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(backToMenuKeyboard());
        execute(message);
    }


    private void handleTextDescription(Long chatId, String prompt, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        if (!rateLimiterService.tryConsume(chatId)) {
            SendMessage rateLimitMsg = new SendMessage(String.valueOf(chatId),
                    "Превышен лимит запросов. Пожалуйста, подождите и попробуйте позже.");
            execute(rateLimitMsg);
            return;
        }
        if (user.getBalance() <= 0) {
            sendMainMenu(chatId, "У вас нет доступных генераций. Пополните баланс.");
            return;
        }
        try {
            userService.consumeOneGeneration(user);
        } catch (IllegalStateException e) {
            sendMainMenu(chatId, "У вас нет доступных генераций. Пополните баланс.");
            return;
        }

        SendMessage waitMsg = new SendMessage(String.valueOf(chatId), "Генерирую видео, пожалуйста подождите... Это может занять несколько минут.");
        execute(waitMsg);
        String format = session.getSelectedFormat();

        session.setState(BotState.INITIAL);

        videoGenerationService.generateVideoFromText(format, prompt)
                .subscribe(
                        url -> {
                            SendVideo msg = new SendVideo(String.valueOf(chatId), new InputFile(url));
                            msg.setCaption("Ваше сгенерированное видео");
                            try {
                                execute(msg);
                                msg.setSupportsStreaming(true);
                                sendMainMenu(chatId, "Генерация завершена. Выберите следующее действие:");
                            } catch (TelegramApiException e) {
                                log.error("Error sending video", e);
                                SendMessage errorMsg = new SendMessage(String.valueOf(chatId), "Не удалось отправить видео: " + e.getMessage());
                                try {
                                    execute(errorMsg);
                                } catch (TelegramApiException ex) {
                                    log.error("Nested error sending error message", ex);
                                }
                            }
                        },
                        error -> {
                            log.error("Video generation failed", error);
                            SendMessage errorMsg = new SendMessage(String.valueOf(chatId), "Не удалось сгенерировать видео: " + error.getMessage());
                            try {
                                execute(errorMsg);
                                userService.addBalance(user, 1);
                            } catch (TelegramApiException e) {
                                log.error("Error sending error message", e);
                            }
                        }
                );
    }

    private void handleImageUpload(Long chatId, Message message, UserSession session) throws TelegramApiException {
        log.trace("Call handleImageUpload");
        User user = userService.findOrCreateUser(chatId);
        // Apply per-user rate limiting
        if (!rateLimiterService.tryConsume(chatId)) {
            SendMessage rateLimitMsg = new SendMessage(String.valueOf(chatId),
                    "Превышен лимит запросов. Пожалуйста, подождите и попробуйте позже.");
            execute(rateLimitMsg);
            return;
        }
        if (user.getBalance() <= 0) {
            sendMainMenu(chatId, "У вас нет доступных генераций. Пополните баланс.");
            return;
        }
        String fileId = message.getPhoto().stream()
                .max((a, b) -> Integer.compare(a.getFileSize(), b.getFileSize()))
                .map(PhotoSize::getFileId)
                .orElse(null);
        if (fileId == null) {
            SendMessage errMsg = new SendMessage(String.valueOf(chatId), "Не удалось получить файл изображения.");
            execute(errMsg);
            return;
        }
        // consume one generation
        try {
            userService.consumeOneGeneration(user);
        } catch (IllegalStateException e) {
            sendMainMenu(chatId, "У вас нет доступных генераций. Пополните баланс.");
            return;
        }

        SendMessage waitMsg = new SendMessage(String.valueOf(chatId), "Генерирую видео из изображения, пожалуйста подождите...");
        execute(waitMsg);

        try {
            org.telegram.telegrambots.meta.api.methods.GetFile getFileRequest = new org.telegram.telegrambots.meta.api.methods.GetFile();
            getFileRequest.setFileId(fileId);
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFileRequest);
            String filePath = file.getFilePath();
            String imageUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;
            String prompt = message.getCaption() == null ? "" : message.getCaption();

            session.setState(BotState.INITIAL);
            videoGenerationService.generateVideoFromImage("16:9", prompt, imageUrl)
                    .subscribe(bytes -> {
                        SendVideo msg = new SendVideo(String.valueOf(chatId), new InputFile(bytes));
                        msg.setCaption("Ваше сгенерированное видео");
                        try {
                            execute(msg);
                            msg.setSupportsStreaming(true);
                            sendMainMenu(chatId, "Генерация завершена. Выберите следующее действие:");
                        } catch (TelegramApiException e) {
                            log.error("Error sending video", e);
                            SendMessage errorMsg = new SendMessage(String.valueOf(chatId), "Не удалось отправить видео: " + e.getMessage());
                            try {
                                execute(errorMsg);
                                userService.addBalance(user, 1);
                            } catch (TelegramApiException ex) {
                                log.error("Nested error sending error message", ex);
                            }
                        }
                    }, error -> {
                        log.error("Video generation from image failed", error);
                        SendMessage errorMsg = new SendMessage(String.valueOf(chatId), "Не удалось сгенерировать видео из изображения: " + error.getMessage());
                        try {
                            execute(errorMsg);
                            userService.addBalance(user, 1);
                        } catch (TelegramApiException e) {
                            log.error("Nested error sending error message", e);
                        }
                    });
        } catch (TelegramApiException e) {
            log.error("Error fetching file path", e);
            SendMessage errMsg = new SendMessage(String.valueOf(chatId), "Не удалось загрузить изображение: " + e.getMessage());
            execute(errMsg);
            userService.addBalance(user, 1);
        }
    }

    private InlineKeyboardMarkup packageKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("1 видео (10 секунд) 69 руб", "package_1")));
        rows.add(List.of(createButton("5 видео (10 секунд) 300 руб", "package_5")));
        rows.add(List.of(createButton("50 видео (10 секунд) 2500 руб", "package_50")));
        rows.add(List.of(createButton("Получить подарок", "package_gift")));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }


    private InlineKeyboardMarkup mainMenuKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("Сгенерировать видео по тексту", "main_generate_text")));
        rows.add(List.of(createButton("Сгенерировать видео по картинке", "main_generate_image")));
        rows.add(List.of(createButton("Пополнить баланс", "main_recharge")));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup formatKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        // Two buttons in one row
        rows.add(List.of(createButton("16:9", "format_16_9"), createButton("9:16", "format_9_16")));
        rows.add(List.of(createButton("назад", "format_back")));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }


    private InlineKeyboardMarkup backToMenuKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("Главное меню", "menu_back")));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}