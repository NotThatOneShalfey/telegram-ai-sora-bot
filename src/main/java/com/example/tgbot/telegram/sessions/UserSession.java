package com.example.tgbot.telegram.sessions;

import com.example.tgbot.db.User;
import com.example.tgbot.models.configurations.ModelRequestOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
public class UserSession {

    private final User user;
    private final String chatId;
    @Setter
    private LocalDateTime lastActionDateTime;
    private final Map<GenerationModel, ModelRequestOptions> modelsConfiguration = new HashMap<>();
    @Setter
    private PaidPackageEnum paymentInfo = null;
    @Setter
    private ChatContext chatContext;

    public UserSession(User user) {
        this.user = user;
        this.chatId = user.getTelegramId().toString();
        this.chatContext = new ChatContext(ChatState.INITIAL);
    }

}
