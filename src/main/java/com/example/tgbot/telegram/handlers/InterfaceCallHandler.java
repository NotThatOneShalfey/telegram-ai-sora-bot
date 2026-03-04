package com.example.tgbot.telegram.handlers;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.sessions.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterfaceCallHandler {
    private final ObjectProvider<RegistryService> registryServiceProvider;
    private final UserService userService;

    public void handleRequest(UserSession session, String dtoBody, GenerationModel model) {
        IModelRequestOptions requestOptions = session.getCurrentRequestOptionsByModel(model);
        requestOptions.setParametersFromJson(dtoBody);
        registryServiceProvider.getObject().getAdapter(model).makeRequest(session);
    }

}
