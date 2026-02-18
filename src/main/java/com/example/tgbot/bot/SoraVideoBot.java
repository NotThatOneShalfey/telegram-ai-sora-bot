package com.example.tgbot.bot;

import com.example.tgbot.data.BotState;
import com.example.tgbot.data.GenModel;
import com.example.tgbot.data.PaidPackage;
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
import org.telegram.telegrambots.meta.api.methods.*;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
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

    @Value("${telegram.bot.payment-token:}")
    private String providerToken;

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
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
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

            // Если подтверждение оплаты
            if (update.hasPreCheckoutQuery()) {
                log.trace("update has preCheckoutQuery");
                handlePreCheckout(update.getPreCheckoutQuery().getId());
                return;
            }

            if (update.hasMessage()) {
                Message message = update.getMessage();
                Long chatId = message.getChatId();
                UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession(BotState.INITIAL));
                // Обработка стартового сообщения
                if (message.hasText() && message.getText().equalsIgnoreCase("/start")) {
                    handleStart(chatId, session);
                    return;
                }
                // Если завершение оплаты
                if (message.getSuccessfulPayment() != null) {
                    addPackage(chatId, message.getSuccessfulPayment().getInvoicePayload(), session);
                    sendMainMenu(chatId, "Оплата успешно зафиксирована!", session);
                    return;
                }

                if (session.getModel().equals(GenModel.SORA_2)) {
                    if (message.hasText()) {
                        switch (session.getState()) {
                            case WAITING_FOR_TEXT_DESCRIPTION:
                                handleTextDescription(chatId, message.getText(), session);
                                break;
                            default:
                                sendMainMenu(chatId, "Я не понял вашу команду. Пожалуйста, выберите действие из меню.", session);
                        }
                    }
                } else if (session.getModel().equals(GenModel.KLING_3_0)) {
                    boolean hasImage = (message.hasDocument() && message.getDocument().getMimeType().contains("image")) || message.hasPhoto();
                    String text = message.getCaption() != null ? message.getCaption() : message.hasText() ? message.getText() : null;
                    boolean hasText = (text != null);
                    // Если есть и картинка и текст
                    if (hasText && hasImage) {
                        handleImageUpload(chatId, message, session);
                    } else if (hasText && !hasImage) { // Если есть текст, но нет картинки
                        sendMessageWithText(chatId, "Простите, но я не могу найти изображение, прошу пришлите мне изображение вместе с текстом");
                    } else if (!hasText && hasImage) { // Если есть картинка, но нет текста
                        sendMessageWithText(chatId, "Простите, но я не могу найти текст, прошу пришлите мне изображение вместе с текстом");
                    } else { // Если вообще прислали что то странное
                        sendMainMenu(chatId, "Я не понял вашу команду. Пожалуйста, выберите действие из меню.", session);
                    }
                } else if (session.getModel().equals(GenModel.NANO_BANANA)) {
                    boolean hasImage = (message.hasDocument() && message.getDocument().getMimeType().contains("image")) || message.hasPhoto();
                    String text = message.getCaption() != null ? message.getCaption() : message.hasText() ? message.getText() : null;
                    boolean hasText = (text != null);
                    // Если есть и картинка и текст
                    if (hasText && hasImage) {
                        session.setModel(GenModel.NANO_BANANA_EDIT);
                        handleImageUpload(chatId, message, session);
                    } else if (hasText && !hasImage) { // Если есть текст, но нет картинки
                        handleTextDescription(chatId, message.getText(), session);
                    } else if (!hasText && hasImage) { // Если есть картинка, но нет текста
                        sendMessageWithText(chatId, "Простите, но я не могу найти текст, прошу пришлите мне текст или изображение вместе с текстом");
                    } else { // Если вообще прислали что то странное
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
        sessions.put(chatId, new UserSession(BotState.WAITING_FOR_PACKAGE_SELECTION));
        /*
        String text = "\uD83C\uDFAC Привет! Я Sora 2 — твой ИИ для создания видео. " +
                "Я могу сгенерировать 10-секундный ролик по твоему описанию или картинке.\n" +
                "\uD83D\uDCA1 Как это работает:\n" +
                "1️⃣ Отправь мне текст или изображение с идеей видео.\n" +
                "2️⃣ Я превращу твою идею в короткий красивый ролик.";

         */
        String text = "\uD83D\uDE80 Добро пожаловать в CreatorLabAI\n" +
                "\n" +
                "Твоя AI-студия для создания контента прямо в Telegram.\n" +
                "\n" +
                "Здесь ты можешь:\n" +
                "\n" +
                "\uD83C\uDFAC Создать видео по тексту (Sora 2)\n" +
                "Опиши идею — получи готовый ролик.\n" +
                "\n" +
                "\uD83D\uDDBC Создать изображение (Nano banana)\n" +
                "Обложки, аватары, иллюстрации, сцены — за один запрос.\n" +
                "\n" +
                "\uD83C\uDFA5 Оживить изображение (Kling 3.0)\n" +
                "Преврати картинку в динамичное видео.\n" +
                "\n" +
                "Выбирай инструмент ниже и начинай создавать \uD83D\uDC47";
        SendMessage message = new SendMessage();
        message.setReplyMarkup(mainMenuKeyboard(user.isBonusReceived()));
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        session.putMessageHistory(message);
        execute(message);
    }

    private void handleCallback(CallbackQuery callback) throws TelegramApiException {
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();
        UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession(BotState.INITIAL));
        User user = userService.findOrCreateUser(chatId);
        log.debug("Received callback {} from {}", data, chatId);
        switch (data.toLowerCase()) {
            case "package_100":
                sendInvoice(chatId, PaidPackage.PACKAGE_100);
                break;
            case "package_550":
                sendInvoice(chatId, PaidPackage.PACKAGE_550);
                break;
            case "package_1200":
                sendInvoice(chatId, PaidPackage.PACKAGE_1200);
                break;
            case "package_6500":
                sendInvoice(chatId, PaidPackage.PACKAGE_6500);
                break;
            case "package_gift":
                if (!user.isBonusReceived()) {
                    userService.addGift(user);
                    sendAfterGift(chatId, user.getBalance(), session);
                }
                break;
            case "gen_nano_banana":
                if (user.getBalance() <= 0) {
                    sendMainMenu(chatId, "⚠ У вас закончились генерации для создания видео.\n" +
                            "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
                } else {
                    session.setState(BotState.WAITING_FOR_FORMAT_SELECTION);
                    session.setModel(GenModel.NANO_BANANA);
                    sendFormatSelection(chatId, user.getBalance(), session);
                }
                break;
            case "gen_sora_2":
                if (user.getBalance() <= 0) {
                    sendMainMenu(chatId, "⚠ У вас закончились генерации для создания видео.\n" +
                            "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
                } else {
                    session.setState(BotState.WAITING_FOR_FORMAT_SELECTION);
                    session.setModel(GenModel.SORA_2);
                    sendFormatSelection(chatId, user.getBalance(), session);
                }
                break;
            case "gen_kling_3_0":
                if (user.getBalance() <= 0) {
                    sendMainMenu(chatId, "⚠ У вас закончились генерации для создания видео.\n" +
                            "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
                } else {
                    session.setModel(GenModel.KLING_3_0);
                    session.setState(BotState.WAITING_FOR_FORMAT_SELECTION);
                    sendFormatSelection(chatId, user.getBalance(), session);
                }
                break;
            case "main_recharge":
                session.setState(BotState.WAITING_FOR_PACKAGE_SELECTION);
                sendPaymentInfo(chatId, user.getBalance(), session);
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
                sendMainMenu(chatId, null, session);
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
        msg.setReplyMarkup(mainMenuKeyboard(user.isBonusReceived()));
        msg.setParseMode(ParseMode.MARKDOWNV2);
        msg.disableWebPagePreview();
        session.putMessageHistory(msg);
        execute(msg);
    }

    // TODO Это убрать как только оплату прикрутим
    private void sendAfterPurchaseTemp(Long chatId, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        sessions.get(chatId).setState(BotState.INITIAL);
        String text = "Простите, оплата временно недоступна.";
        text = text + getQuotaMessageEntityElement(user.getBalance());
        SendMessage msg = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        msg.setReplyMarkup(mainMenuKeyboard(user.isBonusReceived()));
        msg.disableWebPagePreview();
        session.putMessageHistory(msg);
        execute(msg);
    }

    private void sendAfterVideoGeneration(Long chatId, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        String text = "⏳ Отлично! Я получил твоё описание. Генерация займёт ~3 минуты. Как только будет готово, я пришлю тебе сообщение! \uD83C\uDFAC"
                + getQuotaMessageEntityElement(user.getBalance());
        SendMessage msg = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        msg.setParseMode(ParseMode.MARKDOWNV2);
        msg.setReplyMarkup(backButton());
        msg.disableWebPagePreview();
        session.putMessageHistory(msg);
        execute(msg);
    }

    private void sendAfterGift(Long chatId, int balance, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        sessions.get(chatId).setState(BotState.INITIAL);
        String text = "\uD83C\uDF81 Поздравляем!\n\nТы получил 100 монет!✨\n1 Монета = 1 Рублю\nТеперь ты можешь творить!"
                + getQuotaMessageEntityElement(balance);
//        String text = String.format("Поздравляем, у вас доступно %d видео\n\n" +
//                "Тут ты можешь посмотреть примеры и шаблоны : ССЫЛКА\n" +
//                "Инструкция как пользоваться ботом: ССЫЛКА", user.getBalance());
        SendMessage msg = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        msg.setParseMode(ParseMode.MARKDOWNV2);
        msg.setReplyMarkup(mainMenuKeyboard(user.isBonusReceived()));
        msg.disableWebPagePreview();
        session.putMessageHistory(msg);
        execute(msg);
    }

    private void addPackage(Long chatId, String packageName, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        userService.addBalance(user, PaidPackage.getPackagePriceByName(packageName));
    }


    private void sendMainMenu(Long chatId, String text, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        if (text == null) {
            text = "\uD83D\uDE80 Добро пожаловать в CreatorLabAI\n" +
                    "\n" +
                    "Твоя AI-студия для создания контента прямо в Telegram.\n" +
                    "\n" +
                    "Здесь ты можешь:\n" +
                    "\n" +
                    "\uD83C\uDFAC Создать видео по тексту (Sora 2)\n" +
                    "Опиши идею — получи готовый ролик.\n" +
                    "\n" +
                    "\uD83D\uDDBC Создать изображение (Nano banana)\n" +
                    "Обложки, аватары, иллюстрации, сцены — за один запрос.\n" +
                    "\n" +
                    "\uD83C\uDFA5 Оживить изображение (Kling 3.0)\n" +
                    "Преврати картинку в динамичное видео.\n" +
                    "\n" +
                    "Выбирай инструмент ниже и начинай создавать \uD83D\uDC47";
        }
        text = text + getQuotaMessageEntityElement(user.getBalance());
        SendMessage message = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        message.setParseMode(ParseMode.MARKDOWNV2);
        message.setReplyMarkup(mainMenuKeyboard(user.isBonusReceived()));
        message.disableWebPagePreview();
        session.putMessageHistory(message);
        execute(message);
    }

    private void sendAfterGeneration(Long chatId, String prompt, UserSession session) throws TelegramApiException {
        User user = userService.findOrCreateUser(chatId);
        String text = "";
        if (session.getModel().equals(GenModel.NANO_BANANA_EDIT) || session.getModel().equals(GenModel.NANO_BANANA)) {
            text = "✅ Изображение готово!\n\uD83D\uDCBE Промпт:\n > " + prompt;
        } else {
            text = "✅ Видео готово!\n\uD83D\uDCBE Промпт:\n > " + prompt;
        }
        SendMessage message = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        message.setParseMode(ParseMode.MARKDOWNV2);
        message.setReplyMarkup(secondaryMenuKeyboard(user.isBonusReceived()));
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
        String text = "";
        switch (session.getModel()) {
            case KLING_3_0 -> text = "\uD83C\uDFA5 Kling 3.0 — генерация видео по изображению\n" +
                    "\n" +
                    "Kling 3.0 — это AI-модель, которая превращает статичное изображение в динамичную сцену с реалистичной анимацией, движением камеры и кинематографичным эффектом.\n" +
                    "\n" +
                    "Чтобы создать видео:\n" +
                    "\n" +
                    "1\uFE0F⃣ Прикрепи изображение\n" +
                    "2\uFE0F⃣ Напиши, как оно должно ожить\n" +
                    "\n" +
                    "Отправь картинку и текст в одном сообщении.\n" +
                    "\n" +
                    "\uD83D\uDCB8 СТОИМОСТЬ: 150 монет \uD83D\uDCB8\n" +
                    "\n" +
                    "\uD83E\uDE991 монета = 1 рубль \uD83E\uDE99";
            case SORA_2 -> text = "\uD83C\uDFAC Sora 2 — генерация видео по тексту\n" +
                    "\n" +
                    "Sora 2 создаёт полноценные видеосцены по вашему описанию: с реалистичным движением, освещением, атмосферой и кинематографичным качеством.\n" +
                    "\n" +
                    "Чтобы создать видео — просто отправьте текстовое описание.\n" +
                    "\n" +
                    "\uD83D\uDCB8 СТОИМОСТЬ: 75 монет \uD83D\uDCB8\n" +
                    "\n" +
                    "\uD83E\uDE991 монета = 1 рубль \uD83E\uDE99";
            case NANO_BANANA -> text = "\uD83D\uDDBC Nano Banana — генерация изображений\n" +
                    "\n" +
                    "Отправь:\n" +
                    "\n" +
                    "✍\uFE0F Текстовый промпт — и я создам изображение с нуля\n" +
                    "или\n" +
                    "\uD83D\uDDBC Промпт + картинку — чтобы изменить или доработать загруженное изображение\n" +
                    "\n" +
                    "Просто отправь описание одним сообщением.\n" +
                    "Если хочешь изменить конкретную картинку — прикрепи её вместе с текстом.\n" +
                    "\n" +
                    "\uD83D\uDCB8 СТОИМОСТЬ: 20 монет \uD83D\uDCB8\n" +
                    "\n" +
                    "\uD83E\uDE991 монета = 1 рубль \uD83E\uDE99";
        }
        text = text + getQuotaMessageEntityElement(balance);
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

    private void sendMessageWithText(Long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
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
        if (userService.checkBalanceBeforeGeneration(user, session)) {
            sendMainMenu(chatId, "⚠ У вас закончились монеты для создания видео.\n" +
                    "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
            return;
        }
        // Посылаем ответ, если все нормально
        sendAfterVideoGeneration(chatId, session);
        session.setState(BotState.INITIAL);
        videoGenerationService.generateFromPrompt(session.getModel(), session.getSelectedFormat(), prompt)
                .subscribe(
                        url -> {
                            try {
                                if (session.getModel().equals(GenModel.SORA_2)) {
                                    SendVideo vid = new SendVideo(String.valueOf(chatId), new InputFile(url));
                                    vid.setSupportsStreaming(true);
                                    userService.consumeOneGeneration(user, session);
                                    execute(vid);
                                } else if (session.getModel().equals(GenModel.NANO_BANANA)) {
                                    SendPhoto photo = new SendPhoto(String.valueOf(chatId), new InputFile(url));
                                    userService.consumeOneGeneration(user, session);
                                    execute(photo);
                                }
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
            sendMainMenu(chatId, "⚠ У вас закончились монеты для создания видео.\n" +
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
        if (userService.checkBalanceBeforeGeneration(user, session)) {
            sendMainMenu(chatId, "⚠ У вас закончились монеты для создания видео.\n" +
                    "\uD83D\uDC8EПожалуйста пополните баланс\uD83D\uDC8E", session);
            return;
        }
        // Посылаем ответ, если все нормально
        sendAfterVideoGeneration(chatId, session);
        session.setState(BotState.INITIAL);

        try {
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(fileId);
            File file = execute(getFileRequest);
            String filePath = file.getFilePath();
            String imageUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;

            session.setState(BotState.INITIAL);
            videoGenerationService.generateFromPromptAndImage(session.getModel(), session.getSelectedFormat(), prompt, imageUrl)
                    .subscribe(bytes -> {
                        try {
                            if (session.getModel().equals(GenModel.KLING_3_0)) {
                                SendVideo vid = new SendVideo(String.valueOf(chatId), new InputFile(bytes));
                                vid.setSupportsStreaming(true);
                                userService.consumeOneGeneration(user, session);
                                execute(vid);
                            } else if (session.getModel().equals(GenModel.NANO_BANANA_EDIT)) {
                                SendPhoto photo = new SendPhoto(String.valueOf(chatId), new InputFile(bytes));
                                userService.consumeOneGeneration(user, session);
                                execute(photo);
                            }
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
        Pattern thirdPartyContentViolation = Pattern.compile("third-party content");
        String errorMessage = "\uD83D\uDEA7 Генерация временно недоступна \uD83D\uDEA7\n" +
                "Мы уже работаем над этим - попробуйте чуть позже или обратитесь в поддержку @helper_sora2";
        if (sensitiveContentPattern.matcher(reason).find()) {
            errorMessage = "\uD83D\uDD12 Ваш запрос заблокирован системой безопасности.\n" +
                    "Похоже, в тексте есть фразы, которые модели нельзя генерировать.\n" +
                    "Попробуйте переформулировать без чувствительного контента \uD83D\uDE4F";
        } else if (photorealisticPeoplePattern.matcher(reason).find()) {
            errorMessage = "Простите, но мы пока не можем генерировать видео по фото реальных людей. Мы исправимся, а пока попробуйте сгенерировать что-нибудь другое.";
        } else if (thirdPartyContentViolation.matcher(reason).find()) {
            errorMessage = "Простите, но мы не можем генерировать видео с персонажами, защищенными авторскими правами, попробуйте сгенерировать что-нибудь другое.";
        }
        return errorMessage;
    }

    private InlineKeyboardMarkup packageKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (PaidPackage p : PaidPackage.values()) {
            rows.add(List.of(createButton("%d монет - %d ₽".formatted(p.getAmount(), p.getPrice()), p.toString())));
        }
        rows.add(List.of(createButton("Главное меню", "menu_back")));
        rows.add(List.of(getSupportButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private void sendPaymentInfo(long chatId, int balance, UserSession session) throws TelegramApiException {
        String text = "\uD83D\uDCB3 Пополнение баланса\n" +
                "\n" +
                "1 монета = 1 ₽\n" +
                "\n" +
                "Монеты используются для генерации:\n" +
                "\uD83C\uDFAC видео\n" +
                "\uD83D\uDDBC изображений\n" +
                "\uD83C\uDFA5 анимации изображений\n" +
                "\n" +
                "Выбери подходящий пакет ниже \uD83D\uDC47\n" +
                "\n" +
                "\uD83D\uDCA1 Чем больше пакет — тем выгоднее и удобнее для активной работы.";
        text = text + getQuotaMessageEntityElement(balance);
        SendMessage pkgMsg = new SendMessage(String.valueOf(chatId), makeCharacterEscapingForMarkdown(text));
        pkgMsg.setReplyMarkup(packageKeyboard());
        pkgMsg.setParseMode(ParseMode.MARKDOWNV2);
        pkgMsg.disableWebPagePreview();
        execute(pkgMsg);
    }


    private InlineKeyboardMarkup mainMenuKeyboard(boolean withoutGiftButton) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("Создать видео по тексту (Sora 2)", "gen_sora_2")));
        rows.add(List.of(createButton("Создать видео по картинке (Kling 3.0)", "gen_kling_3_0")));
        rows.add(List.of(createButton("Создать изображение (Nano banana)", "gen_nano_banana")));
        rows.add(List.of(createButton("Пополнить баланс", "main_recharge")));
        if (!withoutGiftButton) {
            rows.add(List.of(createButton("\uD83C\uDF81Получить подарок\uD83C\uDF81", "package_gift")));
        }
        rows.add(List.of(getSupportButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup secondaryMenuKeyboard(boolean withoutGiftButton) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createButton("Создать новое видео по тексту (Sora 2)", "gen_sora_2")));
        rows.add(List.of(createButton("Создать новое видео по картинке (Kling 3.0)", "gen_kling_3_0")));
        rows.add(List.of(createButton("Создать новое изображение (Nano banana)", "gen_nano_banana")));
        rows.add(List.of(createButton("Пополнить баланс", "main_recharge")));
        if (!withoutGiftButton) {
            rows.add(List.of(createButton("\uD83C\uDF81Получить подарок\uD83C\uDF81", "package_gift")));
        }
        rows.add(List.of(getSupportButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup formatKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        // Two buttons in one row
        rows.add(List.of(createButton("\uD83D\uDDA5️ Горизонтальное", "format_9_16"), createButton("\uD83D\uDCF1 Вертикальное", "format_16_9")));
        rows.add(List.of(createButton("Главное меню", "menu_back")));
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
        rows.add(List.of(createButton("Главное меню", "menu_back")));
        //rows.add(List.of(createButton("Назад", "format_back")));
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
        return "\n______________________________________\n\uD83D\uDC8EУ вас осталось : %d монет.\n\uD83D\uDCE9 Примеры и советы: https://t.me/sora2examples".formatted(balance);
    }

    private String makeCharacterEscapingForMarkdown(String str) {
        Set<Character> charsToEscape = new HashSet<>(Arrays.asList('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'));
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (charsToEscape.contains(c)) {
                sb.append("\\");
            }
            sb.append(c);
        }
        log.trace("Сообщение после установки экранирования: {}", sb);
        return sb.toString();
    }

    private void sendInvoice(Long chatId, PaidPackage pack) {
        String title = "Покупка пакета";
        String description = "Покупка монет в CreatorLabAi";
        String payload = pack.getPackageName(); // Можно использовать для идентификации заказа

        List<LabeledPrice> prices = new ArrayList<>();
        prices.add(new LabeledPrice("Пакет " + pack.getPackageName(), pack.getPrice()*100)); // цена в копейках (например, 500 = 5.00 у валюты в копейках)
        SendInvoice invoice = SendInvoice.builder()
                .chatId(chatId.toString())
                .title(title)
                .description(description)
                .payload(payload)
                .providerToken(providerToken)
                .startParameter("test") // параметр для запусков
                .prices(prices)
                .currency("RUB") // валюта
                .build();
        try {
            execute(invoice);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handlePreCheckout(String preCheckoutQueryId) {
        // Подтверждаем оплату
        long execTime = 0L;
        try {
            AnswerPreCheckoutQuery answer = AnswerPreCheckoutQuery.builder()
                    .preCheckoutQueryId(preCheckoutQueryId)
                    .ok(true)
                    .build();
            long startTime = System.currentTimeMillis();
            Object o = execute(answer);
            log.trace("Execute answer = {}", o.toString());
            long endTime = System.currentTimeMillis();
            execTime = endTime - startTime;
            log.trace("AnswerPreCheckoutQuery: {}", answer);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке подтверждения", e);
        } finally {
            log.trace("Exec time in millis: {}", execTime);
        }
    }

    private InlineKeyboardButton getSupportButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Поддержка");
        button.setUrl("t.me/helper_sora2?text=");
        return button;
    }
}