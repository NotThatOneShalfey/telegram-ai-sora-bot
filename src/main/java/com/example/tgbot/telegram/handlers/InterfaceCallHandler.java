package com.example.tgbot.telegram.handlers;

import com.example.tgbot.registry.AdapterRegistry;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InterfaceCallHandler {
    private final AdapterRegistry adapterRegistry;
    private final UserService userService;

    public Optional<String> handleRequest(UserSession session, String dtoBody, GenerationModel model) {
        IModelRequestOptions requestOptions = session.getCurrentRequestOptionsByModel(model);
        requestOptions.setParametersFromJson(dtoBody);
        return adapterRegistry.getAdapter(model).makeRequest(session);
    }

}
