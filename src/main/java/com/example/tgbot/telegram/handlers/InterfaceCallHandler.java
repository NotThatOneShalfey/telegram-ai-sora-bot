package com.example.tgbot.telegram.handlers;

import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.configurations.dto.WebSubmitResult;
import com.example.tgbot.service.PriceRegistryService;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.registry.AdapterRegistry;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    public Optional<WebSubmitResult> handleRequest(UserSession session, String dtoBody, GenerationModel model) {
        IModelRequestOptions requestOptions = session.getCurrentRequestOptionsByModel(model);
        requestOptions.setParametersFromJson(dtoBody);

        int price = priceRegistryService.calculatePrice(model, requestOptions, session.getUser());
        if (!userService.checkBalanceBeforeGeneration(session, price)) {
            return Optional.empty();
        }

        return adapterRegistry.getAdapter(model).makeRequest(session)
                .map(taskId -> {
                    int balance = session.getUser().getBalance();
                    return new WebSubmitResult(taskId, balance);
                });
    }
}
