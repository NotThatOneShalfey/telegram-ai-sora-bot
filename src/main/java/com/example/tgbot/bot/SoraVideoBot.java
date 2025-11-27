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
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;


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
                Message message = update.getMessage();
                Long chatId = message.getChatId();
                UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession(BotState.INITIAL, null));
                // Обработка стартового сообщения
                if (message.hasText() && message.getText().equalsIgnoreCase("/start")) {
                    handleStart(chatId, session);
                    return;
                }

                // Если в сообщении есть документ (так можно посылать фото) и указано, что это image
                if ((message.hasDocument() && message.getDocument().getMimeType().contains("image"))
                        || message.hasPhoto()) {
                    if (session.getState() == BotState.WAITING_FOR_IMAGE_UPLOAD) {
                        handleImageUpload(chatId, message, session);
                    } else {
                        sendMainMenu(chatId, "Фото получено, но я ожидаю другую команду. Выберите действие из меню.", session);
                    }
                } else if (message.hasText()) { // Если нет документа или фото, но есть текст
                    switch (session.getState()) {
                        case WAITING_FOR_TEXT_DESCRIPTION:
                            handleTextDescription(chatId, message.getText(), session);
                            break;
                        default:
                            // unknown message in current state
                            sendMainMenu(chatId, "Я не понял вашу команду. Пожалуйста, выберите действие из меню.", session);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }


    private void handleStart(Long chatId, UserSession session) throws TelegramApiException {
        // Persist or retrieve the user
        User user = userService.findOrCreateUser(chatId);
        sessions.put(chatId, new UserSession(BotState.WAITING_FOR_PACKAGE_SELECTION, null));
        String text = "\uD83C\uDFAC Привет! Я Sora 2 — твой ИИ для создания видео. " +
                "Я могу сгенерировать 10-секундный ролик по твоему описанию или картинке.\n" +
                "\uD83D\uDCA1 Как это работает:\n" +
                "1️⃣ Отправь мне текст или изображение с идеей видео.\n" +
                "2️⃣ Я превращу твою идею в короткий красивый ролик.\n" +
                "\uD83D\uDCB3 Чтобы начать, нажми одну из кнопок ниже для оплаты:";
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(packageKeyboard());
        session.putMessageHistory(message);
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
                //userService.addBalance(user, 1);
                //sendAfterPurchase(chatId, 1, session);
                sendAfterPurchaseTemp(chatId, session);
                break;
            case "package_5":
//                userService.addBalance(user, 5);
//                sendAfterPurchase(chatId, 5, session);
                sendAfterPurchaseTemp(chatId, session);
                break;
            case "package_50":
//                userService.addBalance(user, 50);
//                sendAfterPurchase(chatId, 50, session);
                sendAfterPurchaseTemp(chatId, session);
                break;
            case "package_gift":
                userService.addBalance(user, 1);
                sendAfterGift(chatId, user.getBalance(), session);
                break;
            case "main_generate_text":
                if (user.getBalance() <= 0) {
                    sendMainMenu(chatId, "⚠ У вас закончились генерации для создания видео.\n" +
                            "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
                } else {
                    session.setState(BotState.WAITING_FOR_FORMAT_SELECTION);
                    sendFormatSelection(chatId, user.getBalance(), session);
                }
                break;
            case "main_generate_image":
                if (user.getBalance() <= 0) {
                    sendMainMenu(chatId, "⚠ У вас закончились генерации для создания видео.\n" +
                            "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
                } else {
                    session.setState(BotState.WAITING_FOR_IMAGE_UPLOAD);
                    session.setSelectedFormat(null);
                    sendImageUploadPrompt(chatId, user.getBalance(), session);
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
                sendDescriptionPrompt(chatId, user.getBalance(), session);
                break;
            case "format_9_16":
                session.setSelectedFormat("9:16");
                session.setState(BotState.WAITING_FOR_TEXT_DESCRIPTION);
                sendDescriptionPrompt(chatId, user.getBalance(), session);
                break;
            case "format_back":
                session.setState(BotState.INITIAL);
                sendLastMessage(chatId, session);
                break;
            case "menu_back":
                session.setState(BotState.INITIAL);
                sendMainMenu(chatId, "Возвращаюсь в главное меню.", session);
                break;
            default:

                break;
        }

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callback.getId());
        execute(answer);
    }

    private void sendAfterPurchase(Long chatId, int purchasedAmount, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        sessions.get(chatId).setState(BotState.INITIAL);
        String text = String.format("""
                \uD83C\uDF89 Спасибо за оплату!

                Ты пополнил баланс и получил %d генераций видео.
                ✨ Теперь можно создавать ролики по тексту или картинке — 10 секунд, 720p.
                """, purchasedAmount);
        text = text + getQuotaMessageEntityElement(user.getBalance());
//        String text = String.format("Поздравляем, у вас доступно %d видео\n\n" +
//                "Тут ты можешь посмотреть примеры и шаблоны : ССЫЛКА\n" +
//                "Инструкция как пользоваться ботом: ССЫЛКА", user.getBalance());
        SendMessage msg = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        msg.setParseMode(ParseMode.MARKDOWNV2);
        msg.setReplyMarkup(mainMenuKeyboard());
        msg.disableWebPagePreview();
        session.putMessageHistory(msg);
        execute(msg);
    }

    // TODO Это убрать как только оплату прикрутим
    private void sendAfterPurchaseTemp(Long chatId, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        sessions.get(chatId).setState(BotState.INITIAL);
        String text = "Простите, оплата временно недоступна.";
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setReplyMarkup(mainMenuKeyboard());
        session.putMessageHistory(msg);
        execute(msg);
    }

    private void sendAfterVideoGeneration(Long chatId, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        String text = "⏳ Отлично! Я получил твоё описание. Генерация видео займёт ~3 минуты. Как только ролик будет готов, я пришлю его сюда! \uD83C\uDFAC"
                + getQuotaMessageEntityElement(user.getBalance());
        SendMessage msg = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        msg.setParseMode(ParseMode.MARKDOWNV2);
        msg.setReplyMarkup(secondaryMenuKeyboard());
        msg.disableWebPagePreview();
        session.putMessageHistory(msg);
        execute(msg);
    }

    private void sendAfterGift(Long chatId, int balance, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        sessions.get(chatId).setState(BotState.INITIAL);
        String text = "\uD83C\uDF81 Поздравляем!\n\nТы получил 1 бесплатную генерацию видео!✨\nТеперь можешь создать ролик по тексту или картинке."
                + getQuotaMessageEntityElement(balance);
//        String text = String.format("Поздравляем, у вас доступно %d видео\n\n" +
//                "Тут ты можешь посмотреть примеры и шаблоны : ССЫЛКА\n" +
//                "Инструкция как пользоваться ботом: ССЫЛКА", user.getBalance());
        SendMessage msg = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        msg.setParseMode(ParseMode.MARKDOWNV2);
        msg.setReplyMarkup(mainMenuKeyboard());
        msg.disableWebPagePreview();
        session.putMessageHistory(msg);
        execute(msg);
    }


    private void sendMainMenu(Long chatId, String text, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        if (text == null) {
            text = "Главное меню";
        }
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(mainMenuKeyboard());
        session.putMessageHistory(message);
        execute(message);
    }

    private void sendAfterGeneration(Long chatId, String prompt, UserSession session) throws TelegramApiException {
        String text = "✅ Видео готово!\n\uD83D\uDCBE Промпт:\n> " + prompt;
        SendMessage message = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        message.setParseMode(ParseMode.MARKDOWNV2);
        message.setReplyMarkup(secondaryMenuKeyboard());
        session.putMessageHistory(message);
        execute(message);
    }

    private void sendFormatSelection(Long chatId, int balance, UserSession session) throws TelegramApiException {
        String text = "\uD83D\uDCFD️Выберите удобный формат\uD83D\uDCFD️";
        SendMessage message = new SendMessage(String.valueOf(chatId), centerText(text, text.length()+20));
        message.setReplyMarkup(formatKeyboard());
        session.putMessageHistory(message);
        execute(message);
    }

    private void sendDescriptionPrompt(Long chatId, int balance, UserSession session) throws TelegramApiException {
        String text = "✏ Отправь мне сообщение и я сгенерирую видео!"
                + getQuotaMessageEntityElement(balance);
//        String text = String.format(
//                "Модель для генерации Sora 2\nУ вас доступно %d генераций\nВведите описание своего видео.\n\n" +
//                "Тут ты можешь посмотреть примеры и шаблоны : ССЫЛКА\n" +
//                "Гайд по генерации видео", balance);
        SendMessage message = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        message.setParseMode(ParseMode.MARKDOWNV2);
        message.setReplyMarkup(backButton());
        message.disableWebPagePreview();
        session.putMessageHistory(message);
        execute(message);
    }

    private void sendImageUploadPrompt(Long chatId, int balance, UserSession session) throws TelegramApiException {
        String text = "✏ Отправь мне сообщение вместе с изображением и я сгенерирую видео!"
                + getQuotaMessageEntityElement(balance);
//        String text = String.format(
//                "Модель для генерации: Sora 2\nУ вас доступно %d генераций\n" +
//                "Отправьте изображение для генерации видео (JPEG, PNG, WEBP).\n\n" +
//                "Тут ты можешь посмотреть примеры и шаблоны : ССЫЛКА", balance);
        SendMessage message = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        message.setParseMode(ParseMode.MARKDOWNV2);
        message.setReplyMarkup(backButton());
        message.disableWebPagePreview();
        session.putMessageHistory(message);
        execute(message);
    }


    private void handleTextDescription(Long chatId, String prompt, UserSession session) throws TelegramApiException {
        // Хардкод на длину промпта
        if (prompt.length() > 9999) {
            SendMessage promptTooLong = new SendMessage(String.valueOf(chatId),
                    "\uD83D\uDCDD Ваш запрос слишком длинный.\n" +
                            "Попробуйте сократить текст до 10000 символов.");
            execute(promptTooLong);
            return;
        }
        User user = userService.findOrCreateUser(chatId);
        if (!rateLimiterService.tryConsume(chatId)) {
            SendMessage rateLimitMsg = new SendMessage(String.valueOf(chatId),
                    "Превышен лимит запросов. Пожалуйста, подождите и попробуйте позже.");
            execute(rateLimitMsg);
            return;
        }
        if (user.getBalance() <= 0) {
            sendMainMenu(chatId, "⚠ У вас закончились генерации для создания видео.\n" +
                    "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
            return;
        }
        try {
            userService.consumeOneGeneration(user);
        } catch (IllegalStateException e) {
            sendMainMenu(chatId, "У вас нет доступных генераций. Пополните баланс.", session);
            return;
        }

        // Посылаем ответ, если все нормально
        sendAfterVideoGeneration(chatId, session);

        String format = session.getSelectedFormat();
        session.setState(BotState.INITIAL);

        videoGenerationService.generateVideoFromText(format, prompt)
                .subscribe(
                        url -> {
                            SendVideo msg = new SendVideo(String.valueOf(chatId), new InputFile(url));
                            //msg.setCaption("Ваше сгенерированное видео");
                            try {
                                execute(msg);
                                msg.setSupportsStreaming(true);
                                sendAfterGeneration(chatId, prompt, session);
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
                            SendMessage errorMsg = new SendMessage(String.valueOf(chatId), processFailedRequest(error.getMessage()));
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
        // Здесь хардкод просто анимирования картинки, без промпта sora не знает, что делать с картинкой
        String prompt = message.getCaption() == null ? "Анимируй" : message.getCaption();
        // Хардкод на длину промпта
        if (prompt.length() > 9999) {
            SendMessage promptTooLong = new SendMessage(String.valueOf(chatId),
                    "\uD83D\uDCDD Ваш запрос слишком длинный.\n" +
                            "Попробуйте сократить текст до 10000 символов.");
            execute(promptTooLong);
            return;
        }
        User user = userService.findOrCreateUser(chatId);
        // Apply per-user rate limiting
        if (!rateLimiterService.tryConsume(chatId)) {
            SendMessage rateLimitMsg = new SendMessage(String.valueOf(chatId),
                    "Превышен лимит запросов. Пожалуйста, подождите и попробуйте позже.");
            execute(rateLimitMsg);
            return;
        }
        if (user.getBalance() <= 0) {
            sendMainMenu(chatId, "⚠ У вас закончились генерации для создания видео.\n" +
                    "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
            return;
        }
        String fileId = null;
        if (message.hasPhoto()) {
            fileId = message.getPhoto().stream()
                    .max((a, b) -> Integer.compare(a.getFileSize(), b.getFileSize()))
                    .map(PhotoSize::getFileId)
                    .orElse(null);
        } else if (message.hasDocument()) {
            fileId = message.getDocument().getFileId();
        }
        if (fileId == null) {
            SendMessage errMsg = new SendMessage(String.valueOf(chatId), "Не удалось получить файл изображения.");
            execute(errMsg);
            return;
        }
        // consume one generation
        try {
            userService.consumeOneGeneration(user);
        } catch (IllegalStateException e) {
            sendMainMenu(chatId, "У вас нет доступных генераций. Пополните баланс.", session);
            return;
        }
        // Посылаем ответ, если все нормально
        sendAfterVideoGeneration(chatId, session);

        try {
            org.telegram.telegrambots.meta.api.methods.GetFile getFileRequest = new org.telegram.telegrambots.meta.api.methods.GetFile();
            getFileRequest.setFileId(fileId);
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFileRequest);
            String filePath = file.getFilePath();
            String imageUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;

            session.setState(BotState.INITIAL);
            videoGenerationService.generateVideoFromImage("16:9", prompt, imageUrl)
                    .subscribe(bytes -> {
                        SendVideo msg = new SendVideo(String.valueOf(chatId), new InputFile(bytes));
                        //msg.setCaption("Ваше сгенерированное видео");
                        try {
                            execute(msg);
                            msg.setSupportsStreaming(true);
                            sendAfterGeneration(chatId, prompt, session);
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
                        SendMessage errorMsg = new SendMessage(String.valueOf(chatId), processFailedRequest(error.getMessage()));
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

    private String processFailedRequest(String reason) {
        Pattern sensitiveContentPattern = Pattern.compile("harassment|discrimination|bullying|prohibited content");
        Pattern photorealisticPeoplePattern = Pattern.compile("photorealistic people");
        String errorMessage = "\uD83D\uDEA7 Генерация временно недоступна \uD83D\uDEA7\n" +
                "Мы уже работаем над этим - попробуйте чуть позже или обратитесь в поддержку @helper_sora2";
        if (sensitiveContentPattern.matcher(reason).find()) {
            errorMessage = "\uD83D\uDD12 Ваш запрос заблокирован системой безопасности.\n" +
                    "Похоже, в тексте есть фразы, которые модели нельзя генерировать.\n" +
                    "Попробуйте переформулировать без чувствительного контента \uD83D\uDE4F";
        } else if (photorealisticPeoplePattern.matcher(reason).find()) {
            errorMessage = "Простите, но мы пока не можем генерировать видео по фото реальных людей. Мы исправимся, а пока попробуйте сгенерировать что-нибудь другое.";
        }
        return errorMessage;
    }

    private InlineKeyboardMarkup packageKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("1 видео (10 секунд) 69 руб", "package_1")));
        rows.add(List.of(createButton("5 видео (10 секунд) 300 руб", "package_5")));
        rows.add(List.of(createButton("50 видео (10 секунд) 2500 руб", "package_50")));
        //rows.add(List.of(createButton("Получить подарок", "package_gift")));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }


    private InlineKeyboardMarkup mainMenuKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("Сгенерировать видео по тексту", "main_generate_text")));
        rows.add(List.of(createButton("Сгенерировать видео по картинке", "main_generate_image")));
        rows.add(List.of(createButton("Пополнить баланс", "main_recharge")));
        rows.add(List.of(createButton("Поддержка", "menu_back")));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup secondaryMenuKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("Сгенерировать новое видео по тексту", "main_generate_text")));
        rows.add(List.of(createButton("Сгенерировать новое видео по картинке", "main_generate_image")));
        rows.add(List.of(createButton("Пополнить баланс", "main_recharge")));
        rows.add(List.of(createButton("Поддержка", "menu_back")));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup formatKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        // Two buttons in one row
        rows.add(List.of(createButton("\uD83D\uDDA5️ Горизонтальное", "format_9_16"), createButton("\uD83D\uDCF1 Вертикальное", "format_16_9")));
        rows.add(List.of(createButton("Назад", "format_back")));
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

    private String centerText(String text, int lineLength) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            int padding = (lineLength - line.length()) / 2;
            sb.append(" ".repeat(Math.max(0, padding)));
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    private InlineKeyboardMarkup backButton() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("Назад", "format_back")));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private void sendLastMessage(Long chatId, UserSession session) throws TelegramApiException {
        SendMessage msg = session.getLastMessageBeforeCall();
        if (msg == null) {
            msg = new SendMessage(String.valueOf(chatId), "Простите, не могу найти историю сообщений...");
        }
        execute(msg);
    }

    private String getQuotaMessageEntityElement(int balance) {
        return "\n\n> \uD83D\uDC8EУ вас осталось : %d генераций. \n> \uD83D\uDCE9 Примеры и советы: https://t.me/sora2examples".formatted(balance);
    }

    private String makeCharacterEscapingForMarkdown(String str) {
        Set<Character> charsToEscape = new HashSet<>(Arrays.asList('_', '*', '[', ']', '(', ')', '~', '`', '>' , '#', '+', '-', '=', '|', '{', '}', '.'));
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (charsToEscape.contains(c)) {
                sb.append("\\");
            }
            sb.append(c);
        }
        return sb.toString();
    }
}