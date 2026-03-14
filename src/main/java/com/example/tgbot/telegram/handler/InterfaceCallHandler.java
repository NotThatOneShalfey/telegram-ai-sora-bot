package com.example.tgbot.telegram.handler;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.dto.api.SubmitOutcome;
import com.example.tgbot.dto.api.WebSubmitResult;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.integration.config.ElevenLabsOptions;
import com.example.tgbot.integration.config.KlingMotionControlOptions;
import com.example.tgbot.registry.AdapterRegistry;
import com.example.tgbot.service.PriceRegistryService;
import com.example.tgbot.service.UploadService;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.session.UserSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class InterfaceCallHandler {
    private final AdapterRegistry adapterRegistry;
    private final UserService userService;
    private final PriceRegistryService priceRegistryService;

    private final UploadService uploadService;

    public InterfaceCallHandler(@Lazy AdapterRegistry adapterRegistry, UserService userService,
                                PriceRegistryService priceRegistryService, UploadService uploadService) {
        this.adapterRegistry = adapterRegistry;
        this.userService = userService;
        this.priceRegistryService = priceRegistryService;
        this.uploadService = uploadService;
    }

    public SubmitOutcome handleRequest(UserSession session, String dtoBody, GenerationModel model) {
        IModelRequestOptions requestOptions = session.getCurrentRequestOptionsByModel(model);
        requestOptions.setParametersFromJson(dtoBody);

        if (model == GenerationModel.KLING_3_MOTION_CONTROL && requestOptions instanceof KlingMotionControlOptions mc) {
            String orientation = mc.getCharacterOrientation() != null ? mc.getCharacterOrientation() : "video";
            var videoUrls = mc.getVideoUrls();
            if (videoUrls != null) {
                for (String url : videoUrls) {
                    if (uploadService.isOurUrl(url)) {
                        uploadService.validateVideoDurationForMotionControl(url, orientation);
                    }
                }
            }
        }
        if (model == GenerationModel.ELEVENLABS_V3 && requestOptions instanceof ElevenLabsOptions el) {
            int totalChars = el.getTotalChars();
            if (totalChars > ElevenLabsOptions.MAX_TOTAL_CHARS) {
                throw new IllegalArgumentException(
                        "Сумма символов во всех репликах диалога не должна превышать 5000. У вас — %d символов."
                                .formatted(totalChars));
            }
            if (el.getDialogue() == null || el.getDialogue().isEmpty()) {
                throw new IllegalArgumentException("Диалог не может быть пустым. Добавьте хотя бы одну реплику.");
            }
        }

        int price = priceRegistryService.calculatePrice(model, requestOptions, session.getUser());
        if (!userService.checkBalanceBeforeGeneration(session, price)) {
            return SubmitOutcome.fail(ErrorCode.E004);
        }

        var historyRecord = userService.createGenerationHistoryRequested(session.getUser(), model, requestOptions.getRequestInput());
        session.setOperationsHistoryIdForCurrentModel(model, historyRecord.getId());
        session.setRequestSource(TaskSource.WEB);
        try {
            return adapterRegistry.getAdapter(model).makeRequest(session)
                    .map(taskId -> {
                        int balance = session.getUser().getBalance();
                        return SubmitOutcome.ok(new WebSubmitResult(taskId, balance));
                    })
                    .orElseGet(() -> {
                        userService.updateGenerationHistoryToFailed(historyRecord.getId());
                        return SubmitOutcome.fail(ErrorCode.E007);
                    });
        } finally {
            session.setRequestSource(null);
        }
    }
}
