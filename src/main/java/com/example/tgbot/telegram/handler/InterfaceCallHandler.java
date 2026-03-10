package com.example.tgbot.telegram.handler;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.dto.api.SubmitOutcome;
import com.example.tgbot.dto.api.WebSubmitResult;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.registry.AdapterRegistry;
import com.example.tgbot.service.PriceRegistryService;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.session.UserSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class InterfaceCallHandler {
    private final AdapterRegistry adapterRegistry;
    private final UserService userService;
    private final PriceRegistryService priceRegistryService;

    public InterfaceCallHandler(@Lazy AdapterRegistry adapterRegistry, UserService userService,
                                PriceRegistryService priceRegistryService) {
        this.adapterRegistry = adapterRegistry;
        this.userService = userService;
        this.priceRegistryService = priceRegistryService;
    }

    public SubmitOutcome handleRequest(UserSession session, String dtoBody, GenerationModel model) {
        IModelRequestOptions requestOptions = session.getCurrentRequestOptionsByModel(model);
        requestOptions.setParametersFromJson(dtoBody);

        int price = priceRegistryService.calculatePrice(model, requestOptions, session.getUser());
        if (!userService.checkBalanceBeforeGeneration(session, price)) {
            return SubmitOutcome.fail(ErrorCode.E004);
        }

        session.setRequestSource(TaskSource.WEB);
        try {
            return adapterRegistry.getAdapter(model).makeRequest(session)
                    .map(taskId -> {
                        int balance = session.getUser().getBalance();
                        return SubmitOutcome.ok(new WebSubmitResult(taskId, balance));
                    })
                    .orElse(SubmitOutcome.fail(ErrorCode.E007));
        } finally {
            session.setRequestSource(null);
        }
    }
}
